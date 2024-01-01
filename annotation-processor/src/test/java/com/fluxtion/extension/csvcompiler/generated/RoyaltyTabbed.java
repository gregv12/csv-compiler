package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.FieldAccessor;
import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import com.fluxtion.extension.csvcompiler.annotations.Validator;

import java.util.function.BiConsumer;

@CsvMarshaller(
        acceptPartials = false,
        mappingRow = 1,
        headerLines = 0,
        skipCommentLines = true,
        processEscapeSequences = false,
        skipEmptyLines = false,
        fieldSeparator = '\t',
        ignoreQuotes = false,
        trim = true,
        failOnFirstError = false
)
public final class RoyaltyTabbed implements FieldAccessor {
    @ColumnMapping(
            columnName = "latest age",
            columnIndex = -1,
            trimOverride = false,
            optionalField = true,
            defaultValue = "50"
    )
    @Validator(
            validationMethod = "checkAge"
    )
    private int ageInYears;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = "testing"
    )
    @DataMapping(
            conversionMethod = "convert_Name"
    )
    private String name;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = "unknown"
    )
    @DataMapping(
            lookupName = "registeredId"
    )
    @Validator(validationMethod = "checkRegistered")
    private int registered;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    private boolean resident;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    @DataMapping(
            conversionMethod = "toLowerCase"
    )
    private String town;

    @ColumnMapping(
            columnName = "",
            columnIndex = -1,
            trimOverride = false,
            optionalField = false,
            defaultValue = ""
    )
    @DataMapping(
            conversionMethod = "convert_NameAndTown",
            derivedColumn = true
    )
    private String nameAndTown;

    @DataMapping(lookupName = "meta")
    @ColumnMapping(optionalField = true)//, defaultValue = "dataFile")
    private String dataFile;

    public String toLowerCase(CharSequence input) {
        String myString = input.toString();
        return myString.toLowerCase();
    }

    public boolean checkAge(BiConsumer<String, Boolean> validationLog) {
        if(ageInYears > 100){
            validationLog.accept(ageInYears +  " way too old!!", false);
            return false;
        }
        return true;
    }

    public boolean checkRegistered(BiConsumer<String, Boolean> validationLog) {
        if(registered > 4){
            validationLog.accept("Unsupported registration description", false);
            return false;
        }
        return true;
    }

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

    public String convert_Name(CharSequence input) {
        String myString = input.toString();
        return myString.toUpperCase();
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

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getNameAndTown() {
        return nameAndTown;
    }

    public void setNameAndTown(String nameAndTown) {
        this.nameAndTown = nameAndTown;
    }

    public String convert_NameAndTown(CharSequence input) {
        return name + "->" + town;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
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
            case "town":
                return (T)(Object)town;
            case "nameAndTown":
                return (T)(Object)nameAndTown;
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
        toString += "resident: " + resident + ", ";
        toString += "town: " + town + ", ";
        toString += "nameAndTown: " + nameAndTown;
        toString += "}";
        return toString;
    }
}
