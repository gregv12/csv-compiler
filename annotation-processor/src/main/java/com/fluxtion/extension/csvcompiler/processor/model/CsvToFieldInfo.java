/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
    private String converterClassName;
    private String convertConfiguration;
    //default value
    private String defaultMethod;
    private String defaultInstanceId;
    private Object defaultInstance;
    //validator
    private String validatorMethod;
    //lookup
    private String lookupKey;

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

    public void setConverter(String converterClass, String convertConfiguration) {
        if(converterClass==null || converterClass.isBlank()){
        }else{
            this.converterInstanceId = getFieldIdentifier() + "Converter";
            this.converterMethod = converterInstanceId + ".fromCharSequence";
            this.converterClassName = converterClass;
            this.convertConfiguration = convertConfiguration==null?"":convertConfiguration;
        }
    }

    public String getLookupField(){
        return "lookup_" + getFieldName();
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultMethod = targetCalcMethodName+ ".isEmpty()?\"" + defaultValue + "\":";
    }

    public boolean getMandatoryField() {
        return mandatory;
    }

    public boolean isDefaultOptionalField() {
        return defaultMethod != null && !mandatory;
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
        if(isLookupApplied()){
            defaultMethodCalc = getLookupField() + ".apply(" + defaultMethodCalc + ")";
        }
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

        return targetInstanceId + "." + targetCalcMethodName + "("
                + conversion
                + ");";
    }

    public boolean isNamedField() {
        return !indexField;
    }

    public String getFieldIdentifier() {
        if (indexField) {
            fieldIdentifier = "fieldIndex_" + getFieldName();
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
