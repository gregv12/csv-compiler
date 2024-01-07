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
public final class DefaultValueCsvMarshaller extends BaseMarshallerNoBufferCopy<DefaultValue> {

    private static final int HEADER_ROWS = 1;
    private static final int MAPPING_ROW = 1;
    private boolean escaping = false;
    private boolean prevIsQuote = false;
    private final CharSequenceView setColumnName = sequence.view();
    private final CharSequenceView setDefaultValue = sequence.view();
    private int fieldName_columnName = -1;
    private int fieldName_defaultValue = -1;

    public DefaultValueCsvMarshaller() {
        super(false);        
    }

    @Override
    public Class<DefaultValue> targetClass() {
        return DefaultValue.class;
    }

    @Override
    public void init() {
        super.init();
        target = new DefaultValue();
        fieldMap.put(fieldName_columnName, "setColumnName");
        fieldMap.put(fieldName_defaultValue, "setDefaultValue");
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
        target = new DefaultValue();
        try{
            updateFieldIndex();
            fieldIndex = fieldName_columnName;
            setColumnName.subSequenceNoOffset(delimiterIndex[fieldName_columnName], delimiterIndex[fieldName_columnName + 1] - 1);
            target.setColumnName((setColumnName).toString());            
            fieldIndex = fieldName_defaultValue;
            setDefaultValue.subSequenceNoOffset(delimiterIndex[fieldName_defaultValue], delimiterIndex[fieldName_defaultValue + 1] - 1);
            target.setDefaultValue((setDefaultValue).toString());
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
        fieldName_columnName = headers.indexOf("columnName");
        fieldMap.put(fieldName_columnName, "setColumnName");
        if (fieldName_columnName < 0) {
            logHeaderProblem("problem mapping field:'columnName' missing column header, index row:", true, null);
        }
        fieldName_defaultValue = headers.indexOf("defaultValue");
        fieldMap.put(fieldName_defaultValue, "setDefaultValue");
        if (fieldName_defaultValue < 0) {
            logHeaderProblem("problem mapping field:'defaultValue' missing column header, index row:", true, null);
        }
    }

    public void writeHeaders(StringBuilder builder) {
        builder.append("columnName");
        builder.append(',');
        builder.append("defaultValue");
        builder.append('\n');
    }

    public void writeInputHeaders(StringBuilder builder) {
        builder.append("columnName");
        builder.append(',');
        builder.append("defaultValue");
        builder.append('\n');
    }

    public void writeRow(DefaultValue target, StringBuilder builder) {
        builder.append(target.getColumnName());
        builder.append(',');
        builder.append(target.getDefaultValue());
        builder.append('\n');
    }

}
