package com.fluxtion.extension.csvcompiler.process;
//VERSION 2 GENERATION - NO BUFFER COPY
import com.fluxtion.extension.csvcompiler.BaseMarshaller;
import com.fluxtion.extension.csvcompiler.BaseMarshallerNoBufferCopy;
import com.fluxtion.extension.csvcompiler.CharArrayCharSequence;
import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.ValidationLogger;
import com.fluxtion.extension.csvcompiler.CsvProcessingException;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.fluxtion.extension.csvcompiler.converters.Conversion.*;


@AutoService(RowMarshaller.class)
@SuppressWarnings("rawtypes")
public final class LookupTableCsvMarshaller extends BaseMarshallerNoBufferCopy<LookupTable> {

    private static final int HEADER_ROWS = 1;
    private static final int MAPPING_ROW = 1;
    private boolean escaping = false;
    private boolean prevIsQuote = false;
    private final CharSequenceView setTable = sequence.view();
    private final CharSequenceView setKey = sequence.view();
    private final CharSequenceView setValue = sequence.view();
    private int fieldName_table = -1;
    private int fieldName_key = -1;
    private int fieldName_value = -1;

    public LookupTableCsvMarshaller() {
        super(false);        
    }

    @Override
    public Class<LookupTable> targetClass() {
        return LookupTable.class;
    }

    @Override
    public void init() {
        super.init();
        target = new LookupTable();
        fieldMap.put(fieldName_table, "setTable");
        fieldMap.put(fieldName_key, "setKey");
        fieldMap.put(fieldName_value, "setValue");
    }

    @Override
    public boolean charEvent(char character) {
        passedValidation = true;
        char charToTest = previousChar;
        previousChar = character;
        if (!processChar(character)){
            return false;
        }
        if (escaping) {
            emptyRow = false;
            return false;
        }
        if (character == '\r'){
            return processRow();
        }
        if (character == '\n' & charToTest != '\r') {
            return processRow();
        }
        if (character == '\n'){
            writtenLimit--;
            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);
            readPointer--;
            return false;
        }
        if (character == ',') {
            updateFieldIndex();
        }
        emptyRow = false;
        return false;
    }

    private boolean processChar(char character) {
        boolean charTest = firstCharOfField;
        firstCharOfField = false;
        boolean isQuote = character == '"';
        if (!charTest && !escaping) {
            return true;
        }
        if (!escaping & isQuote) {//first quote
            prevIsQuote = false;
            escaping = true;
            writtenLimit--;
            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);
            readPointer--;
            return false;
        } else if (escaping & !prevIsQuote & isQuote) {//possible termination
            prevIsQuote = true;
            return false;
        } else if (escaping & prevIsQuote & !isQuote) {//actual termination
            writtenLimit--;
            System.arraycopy(chars, readPointer, chars, readPointer - 1, chars.length - readPointer);
            readPointer--;
            prevIsQuote = false;
            escaping = false;
        } else if (escaping & prevIsQuote & isQuote) {//an escaped quote
            writtenLimit--;
            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);
            readPointer--;
            prevIsQuote = false;
        } 
        return true;
    }
    
    protected boolean isEscaping(){
       return escaping;
    }
    
    protected boolean isPreviousAQuote(){
       return prevIsQuote;
    }


    @Override
    protected boolean processRow() {
        boolean targetChanged = false;
        rowNumber++;
        if (sequence.charAt(delimiterIndex[0]) == '#'){
            delimiterIndex[fieldIndex] = readPointer + 1;
            fieldIndex = 0;
            return targetChanged;
        }
        if (HEADER_ROWS < rowNumber & emptyRow){
            removeCharFromBuffer();
            logProblem("empty lines are not valid input");
            fieldIndex = 0;
            return targetChanged;
        }
        if (HEADER_ROWS < rowNumber) {
            targetChanged = updateTarget();
        }
        if (rowNumber == MAPPING_ROW) {
            mapHeader();
        }
        fieldIndex = 0;
        delimiterIndex[fieldIndex] = readPointer + 1;
        emptyRow = true;
        return targetChanged;
    }

    private boolean updateTarget() {
        publish = true;
        int length = 0;
        target = new LookupTable();
        try{
            updateFieldIndex();
            fieldIndex = fieldName_table;
            setTable.subSequenceNoOffset(delimiterIndex[fieldName_table], delimiterIndex[fieldName_table + 1] - 1);
            target.setTable((setTable).toString());            
            fieldIndex = fieldName_key;
            setKey.subSequenceNoOffset(delimiterIndex[fieldName_key], delimiterIndex[fieldName_key + 1] - 1);
            target.setKey((setKey).toString());            
            fieldIndex = fieldName_value;
            setValue.subSequenceNoOffset(delimiterIndex[fieldName_value], delimiterIndex[fieldName_value + 1] - 1);
            target.setValue((setValue).toString());
        } catch (Exception e) {
            logException("problem pushing '"
                    + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex + 1] - 1).toString() + "'"
                    + " from row:'" +rowNumber +"'", false, e);
            passedValidation = false;
            return false;
        } finally {
            fieldIndex = 0;
        }
        return publish;
    }

    private void mapHeader() {
        firstCharOfField = true;
        String header = new String(chars).trim().substring(delimiterIndex[0], readPointer);
        header = headerTransformer.apply(header);
        header = header.replace("\"", "");
        List<String> headers = new ArrayList<>();
        for (String colName : header.split(Pattern.quote(","))) {
            headers.add(getIdentifier(colName));
        }
        fieldName_table = headers.indexOf("table");
        fieldMap.put(fieldName_table, "setTable");
        if (fieldName_table < 0) {
            logHeaderProblem("problem mapping field:'table' missing column header, index row:", true, null);
        }
        fieldName_key = headers.indexOf("key");
        fieldMap.put(fieldName_key, "setKey");
        if (fieldName_key < 0) {
            logHeaderProblem("problem mapping field:'key' missing column header, index row:", true, null);
        }
        fieldName_value = headers.indexOf("value");
        fieldMap.put(fieldName_value, "setValue");
        if (fieldName_value < 0) {
            logHeaderProblem("problem mapping field:'value' missing column header, index row:", true, null);
        }
    }

    public void writeHeaders(StringBuilder builder) {
        builder.append("table");
        builder.append(',');
        builder.append("key");
        builder.append(',');
        builder.append("value");
        builder.append('\n');
    }

    public void writeInputHeaders(StringBuilder builder) {
        builder.append("table");
        builder.append(',');
        builder.append("key");
        builder.append(',');
        builder.append("value");
        builder.append('\n');
    }

    public void writeRow(LookupTable target, StringBuilder builder) {
        builder.append(target.getTable());
        builder.append(',');
        builder.append(target.getKey());
        builder.append(',');
        builder.append(target.getValue());
        builder.append('\n');
    }

}
