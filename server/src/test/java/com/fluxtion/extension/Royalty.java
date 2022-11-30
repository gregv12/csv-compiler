package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

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
    @ColumnMapping(
            columnName = "latest age",
            columnIndex = -1,
            trimOverride = false,
            optionalField = true,
            defaultValue = "50"
    )
    private int ageInYears;
    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = "testing"
    )
    private String name;
    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    private int registered;
    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    private boolean resident;
    public int getAgeInYears() {
        return ageInYears;
    }
    public void setAgeInYears(int ageInYears) {
        this.ageInYears = ageInYears;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getRegistered() {
        return registered;
    }
    public void setRegistered(int registered) {
        this.registered = registered;
    }
    public boolean isResident() {
        return resident;
    }
    public void setResident(boolean resident) {
        this.resident = resident;
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
            case "resident":
                return (T)(Object)resident;
            default:
                break;
        }
        return null;
    }
    public String toString() {
        String toString = "Royalty[";
        toString += "ageInYears:'" + ageInYears + "', ";
        toString += "name:'" + name + "', ";
        toString += "registered:'" + registered + "', ";
        toString += "resident:'" + resident + "'";
        toString += "]";
        return toString;
    }
}
