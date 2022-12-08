package com.fluxtion.extension.csvcompiler;

import com.squareup.javapoet.TypeName;
import lombok.SneakyThrows;
import lombok.ToString;

@ToString
public class ColumnMapping {
    private String name;
    private String type = CharSequence.class.getCanonicalName();
    private String csvColumnName = "";
    private int csvIndex = -1;
    private boolean optional = false;
    private boolean trimOverride = false;
    private String defaultValue = "";
    private String converterCode = "";
    private String converterFunction = "";
    private String converter = "";
    private String converterConfiguration = "";
    private String validationFunction = "";
    private boolean derived = false;
    private String lookupTable;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getCsvIndex() {
        return csvIndex;
    }

    public void setCsvIndex(int csvIndex) {
        this.csvIndex = csvIndex;
    }

    public boolean isTrimOverride() {
        return trimOverride;
    }

    public void setTrimOverride(boolean trimOverride) {
        this.trimOverride = trimOverride;
    }

    public String getCsvColumnName() {
        return csvColumnName;
    }

    public void setCsvColumnName(String csvColumnName) {
        this.csvColumnName = csvColumnName;
    }

    public String getConverterCode() {
        return converterCode;
    }

    public void setConverterCode(String converterCode) {
        this.converterCode = converterCode;
    }

    public String getConverterFunction() {
        return converterFunction;
    }

    public void setConverterFunction(String converterFunction) {
        this.converterFunction = converterFunction;
    }

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public String getConverterConfiguration() {
        return converterConfiguration;
    }

    public void setConverterConfiguration(String converterConfiguration) {
        this.converterConfiguration = converterConfiguration;
    }

    public String getValidationFunction() {
        return validationFunction;
    }

    public void setValidationFunction(String validationFunction) {
        this.validationFunction = validationFunction;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public String getLookupTable() {
        return lookupTable;
    }

    public void setLookupTable(String lookupTable) {
        this.lookupTable = lookupTable;
    }

    @SneakyThrows
    public TypeName asTypeName(){
        return CsvChecker.asTypeName(getType());
    }
}
