package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.FieldAccessor;
import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import java.lang.Override;
import java.lang.String;

@CsvMarshaller(
        acceptPartials = true,
        mappingRow = 1,
        headerLines = 0,
        skipCommentLines = true,
        processEscapeSequences = false,
        skipEmptyLines = false,
        fieldSeparator = ',',
        ignoreQuotes = false,
        trim = false,
        failOnFirstError = false
)
public final class Analysis implements FieldAccessor {
    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    private String name;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = true,
            defaultValue = ""
    )
    private int age;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    private double percentage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public <T> T getField(String fieldName) {
        switch(fieldName) {
            case "name":
                return (T)(Object)name;
            case "age":
                return (T)(Object)age;
            case "percentage":
                return (T)(Object)percentage;
            default:
                break;
        }
        return null;
    }

    public String toString() {
        String toString = "Analysis: {";
        toString += "name: " + name + ", ";
        toString += "age: " + age + ", ";
        toString += "percentage: " + percentage;
        toString += "}";
        return toString;
    }
}