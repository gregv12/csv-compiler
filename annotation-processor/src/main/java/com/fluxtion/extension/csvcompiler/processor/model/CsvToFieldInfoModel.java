package com.fluxtion.extension.csvcompiler.processor.model;

public interface CsvToFieldInfoModel {
    String getTargetCalcMethodName();

    String getFieldIdentifier();

    int getFieldIndex();

    String getFieldName();

    boolean isMandatory();

    boolean isDefaultOptionalField();

    String getUpdateTarget();
}
