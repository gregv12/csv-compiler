package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

@CsvMarshaller(processEscapeSequences = true)
public class DefaultValue {
    private String columnName;
    private String defaultValue = "";

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
