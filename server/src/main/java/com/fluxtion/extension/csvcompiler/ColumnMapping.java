package com.fluxtion.extension.csvcompiler;

public class ColumnMapping {
    private String name;
    private int csvIndex = -1;
    private String csvColumnName = "";
    private String type;
    private boolean optional = false;
    private boolean trimOverride = false;
    private String defaultValue = "";

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
}
