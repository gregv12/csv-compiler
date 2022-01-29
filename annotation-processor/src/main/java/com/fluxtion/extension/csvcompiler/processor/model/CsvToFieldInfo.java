/*
 * Copyright (C) 2018 V12 Technology Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.extension.csvcompiler.processor.model;

import lombok.Data;

/**
 * @author Greg Higgins
 */
@Data
public class CsvToFieldInfo implements CsvToFieldInfoModel {

    //Source
    private int fieldIndex;
    private String fieldName;
    private boolean duplicateField = false;
    private boolean fixedWidth = false;
    private boolean mandatory = true;
    private int fixedStart;
    private int fixedLen;
    private int fixedEnd;
    //Target
    private boolean targetIsEnum;
    private String targetArgType;
    private String targetClassName;
    private String targetCalcMethodName;
    private String targetInstanceId;
    private String targetGetMethod;
    //mapping info
    private boolean trim = false;
    //imports
    private boolean indexField;
    private String fieldIdentifier;
    //converter
    private String converterMethod;
    private String converterInstanceId;
    //default value
    private String defaultMethod;
    private String defaultInstanceId;
    private Object defaultInstance;
    //validator
    private String validatorMethod;

    public void setSourceColIndex(int colIndex) {
        this.fieldIndex = colIndex;
        indexField = true;
    }

    public void setSourceFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.fieldIndex = -1;
        indexField = false;
    }

    public void setSourceFixedField(int startIndex, int length) {
        this.fixedStart = startIndex;
        this.fixedLen = length;
        this.fixedEnd = fixedStart + fixedLen;
        this.fieldIndex = startIndex;
        fixedWidth = true;
    }

    public void setTarget(String targetFieldMethodName, boolean targetIsEnum, String targetArgType, String id) {
        targetInstanceId = id;
        this.targetCalcMethodName = targetFieldMethodName;
        this.targetIsEnum = targetIsEnum;
        this.targetArgType = targetArgType;
        getUpdateTarget();
    }

    public void setConverter(String converterMethod) {
        this.converterMethod = converterMethod;
    }

    public void setValidator(String validatorId, String targetGetMethod) {
        validatorMethod = validatorId;
        this.targetGetMethod = targetGetMethod;
    }

    public void setConverter(String instanceId, String converterMethod) {
        this.converterInstanceId = instanceId;
        this.converterMethod = converterInstanceId + "." + converterMethod;
    }

    public void setDefaultValue(String instanceId, String defaultMethod, Object converterInstance) {
        this.defaultInstanceId = instanceId;
        this.defaultInstance = converterInstance;
        this.defaultMethod = defaultInstanceId + "." + defaultMethod;
    }

    public boolean getMandatoryField() {
        return mandatory;
    }

    public boolean isDefaultOptionalField() {
        final boolean test = defaultMethod != null && !mandatory;
        return test;
    }

    public String getValidate() {
        return validatorMethod + ".validate(" + targetInstanceId + "." + targetGetMethod + "(), validationBuffer)";
    }

    public boolean isValidated() {
        return validatorMethod != null;
    }

    public String getUpdateTarget() {
        String defaultMethodCalc = targetCalcMethodName;
        if (defaultMethod != null) {
            defaultMethodCalc = defaultMethod + "(" + targetCalcMethodName + ")";
        }
        String conversion = defaultMethodCalc;
        boolean addConversion = true;

        if (converterMethod != null) {
            addConversion = false;
            conversion = converterMethod + "(" + defaultMethodCalc + ")";
        } else if (targetIsEnum) {
            conversion = targetArgType + ".valueOf(" + defaultMethodCalc + ".toString())";
        } else {
            switch (targetArgType) {
                case "String":
                    conversion += ".toString()";
                    addConversion = false;
                    break;
                case "CharSequence":
                case "StringBuilder":
                    addConversion = false;
                    break;
                case "double":
                    conversion = "atod(" + defaultMethodCalc + ")";
                    break;
                case "float":
                    conversion = "(float)atod(" + defaultMethodCalc + ")";
                    break;
                case "int":
                    conversion = "atoi(" + defaultMethodCalc + ")";
                    break;
                case "byte":
                    conversion = "(byte)atoi(" + defaultMethodCalc + ")";
                    break;
                case "short":
                    conversion = "(short)atoi(" + defaultMethodCalc + ")";
                    break;
                case "char":
                    conversion = "(char)atoi(" + defaultMethodCalc + ")";
                    break;
                case "long":
                    conversion = "atol(" + defaultMethodCalc + ")";
                    break;
                case "boolean":
                    conversion = "atobool(" + defaultMethodCalc + ")";
                    break;
                case "LocalDate":
                    conversion = "LocalDate.parse(" + defaultMethodCalc + ")";
                    addConversion = false;
                    break;
            }
            if (addConversion) {
//                importMap.addStaticImport(Conversion.class);
            }
        }

        String a = targetInstanceId + "." + targetCalcMethodName + "("
                + conversion
                + ");";
        return a;
    }

    public boolean isNamedField() {
        return !indexField;
    }

    public String getFieldIdentifier() {
        if (indexField) {
            fieldIdentifier = "fieldIndex_" + getFieldIndex();
        } else if (fixedWidth) {
            fieldIdentifier = "fixedStart_" + getFieldIndex();
        } else {
            fieldIdentifier = "fieldName_" + getFieldName();
        }
        return fieldIdentifier;
    }

    public String getFieldLenIdentifier() {
        return "fixedStart_" + getFieldIndex() + "_Len_" + getFixedLen();
    }

    public int getFieldLength() {
        if (fixedWidth) {
            return fixedLen;
        } else {
            return -1;
        }
    }
}
