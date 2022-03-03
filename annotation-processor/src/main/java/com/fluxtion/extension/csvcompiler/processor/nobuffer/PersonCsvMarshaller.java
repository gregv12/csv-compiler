package com.fluxtion.extension.csvcompiler.processor.nobuffer;

import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.fluxtion.extension.csvcompiler.converters.Conversion.atoi;
import static com.fluxtion.extension.csvcompiler.converters.Conversion.getIdentifier;

public final class PersonCsvMarshaller extends BaseMarshaller<Person> {

    private static final int HEADER_ROWS = 1;
    private static final int MAPPING_ROW = 1;

    private final CharSequenceView setName = sequence.view();
    private final CharSequenceView setAge = sequence.view();
    private int fieldName_name = -1;
    private int fieldName_age = -1;

    public PersonCsvMarshaller() {
        super(false);
    }

    @Override
    public Class<Person> targetClass() {
        return Person.class;
    }

    @Override
    public void init() {
        super.init();
        target = new Person();
        fieldMap.put(fieldName_name, "setName");
        fieldMap.put(fieldName_age, "setAge");
    }

    @Override
    public boolean charEvent(char character) {
        passedValidation = true;
        char charToTest = previousChar;
        previousChar = character;
        if (character == '\r') {
            return processRow();
        }
        if (character == '\n' & charToTest != '\r') {
            return processRow();
        }
        if (character == '\n') {
            return false;
        }
        if (character == ',') {
            updateFieldIndex();
        }
        emptyRow = false;
        return false;
    }

    @Override
    protected boolean processRow() {
        boolean targetChanged = false;
        emptyRow = true;
        rowNumber++;
        if (sequence.charAt(delimiterIndex[0]) == '#') {
            fieldIndex = 0;
            return targetChanged;
        }
        if (readPointer < 1) {
            logProblem("empty lines are not valid input");
            fieldIndex = 0;
            return targetChanged;
        }
        if (HEADER_ROWS < rowNumber) {
            targetChanged = updateTarget();
        }
        if (rowNumber == MAPPING_ROW) {
            mapHeader();
            fieldIndex = 0;
            delimiterIndex[fieldIndex] = readPointer;
            return targetChanged;
        }
        fieldIndex = 0;
        return targetChanged;
    }

    private boolean updateTarget() {
        boolean publish = true;
        int length = 0;
        target = new Person();
        try {
            updateFieldIndex();
            fieldIndex = fieldName_name;
            setName.subSequenceNoOffset(
                    delimiterIndex[fieldName_name], delimiterIndex[fieldName_name + 1] - 1);
            target.setName(setName.toString());
            fieldIndex = fieldName_age;
            setAge.subSequenceNoOffset(
                    delimiterIndex[fieldName_age], delimiterIndex[fieldName_age + 1] - 1);
            target.setAge(atoi(setAge));
        } catch (Exception e) {
            logException(
                    "problem pushing '"
                            + sequence.subSequence(
                                            delimiterIndex[fieldIndex],
                                            delimiterIndex[fieldIndex + 1] - 1)
                                    .toString()
                            + "'"
                            + " from row:'"
                            + rowNumber
                            + "'",
                    false,
                    e);
            passedValidation = false;
            return false;
        } finally {
            fieldIndex = 0;
        }
        return publish;
    }

    private void mapHeader() {
        firstCharOfField = true;
        String header = new String(chars).trim().substring(0, readPointer);
        header = headerTransformer.apply(header);
        header = header.replace("\"", "");
        List<String> headers = new ArrayList<>();
        for (String colName : header.split(Pattern.quote(","))) {
            headers.add(getIdentifier(colName));
        }
        fieldName_name = headers.indexOf("name");
        fieldMap.put(fieldName_name, "setName");
        if (fieldName_name < 0) {
            logHeaderProblem(
                    "problem mapping field:'name' missing column header, index row:", true, null);
        }
        fieldName_age = headers.indexOf("age");
        fieldMap.put(fieldName_age, "setAge");
        if (fieldName_age < 0) {
            logHeaderProblem(
                    "problem mapping field:'age' missing column header, index row:", true, null);
        }
    }

    public void writeHeaders(StringBuilder builder) {
        builder.append("name");
        builder.append(',');
        builder.append("age");
        builder.append('\n');
    }

    public void writeRow(Person target, StringBuilder builder) {
        builder.append(target.getName());
        builder.append(',');
        builder.append(target.getAge());
        builder.append('\n');
    }
}
