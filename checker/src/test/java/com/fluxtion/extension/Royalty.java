package com.fluxtion.extension;
import com.fluxtion.extension.csvcompiler.FieldAccessor;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import com.fluxtion.extension.csvcompiler.process.ProcessInput;

import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@CsvMarshaller(
        acceptPartials = false,
        mappingRow = 1,
        headerLines = 0,
        skipCommentLines = true,
        processEscapeSequences = false,
        skipEmptyLines = false,
        fieldSeparator = ',',
        ignoreQuotes = false,
        trim = true,
        failOnFirstError = false
)
public final class Royalty implements FieldAccessor {
    private static int cumulativeCount;

    private static HashMap nameMap2 = new HashMap();

    @ColumnMapping(
            columnName = "latest age",
            columnIndex = -1,
            trimOverride = false,
            escapeOutput = false,
            optionalField = true,
            defaultValue = "",
            outputField = true
    )
    private int[] ageInYears;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            escapeOutput = false,
            optionalField = true,
            defaultValue = "",
            outputField = true
    )
    @DataMapping(
            lookupName = "firstname"
    )
    private String name;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            escapeOutput = false,
            optionalField = false,
            defaultValue = "",
            outputField = false
    )
    private String registered;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            escapeOutput = false,
            optionalField = false,
            defaultValue = "",
            outputField = true
    )
    @DataMapping(
            derivedColumn = true
    )
    private int count2;

    public int[] getAgeInYears() {
        return ageInYears;
    }

    public void setAgeInYears(int[] ageInYears) {
        this.ageInYears = ageInYears;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        System.out.println("SET registered");
        this.registered = registered;
    }

    public int getCount2() {
        return count2;
    }

    public void setCount2(int count2) {
        this.count2 = count2;
    }

    @Override
    public <T> T getField(String fieldName) {
        switch(fieldName) {
            case "ageInYears":
                return (T)(Object)ageInYears;
            case "name":
                return (T)(Object)name;
            case "registered":
                return (T)(Object)registered;
            case "count2":
                return (T)(Object)count2;
            default:
                break;
        }
        return null;
    }

    public String toString() {
        String toString = "Royalty: {";
        toString += "ageInYears: " + ageInYears + ", ";
        toString += "name: " + name + ", ";
        toString += "registered: " + registered + ", ";
        toString += "count2: " + count2;
        toString += "}";
        return toString;
    }

    public static void main(String[] args) throws Exception {
        ProcessInput.run((RowMarshaller<?>) Class.forName("com.fluxtion.extension.csvcompilere.generated.RoyaltyCsvMarshaller").getDeclaredConstructor().newInstance());
    }
}
