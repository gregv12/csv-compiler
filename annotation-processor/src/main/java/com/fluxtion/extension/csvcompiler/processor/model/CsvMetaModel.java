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
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All the meta information required to build a csv parser and csv printer for a bean style class.
 */
@Data
public class CsvMetaModel implements CodeGeneratorModel {

    private final ImportMap importMap = ImportMap.newMap();
    private final Map<String, FieldModel> fieldMap = new LinkedHashMap<>();
    private final String marshallerClassName;
    private final String targetClassName;
    private final String packageName;
    private int headerLines = 0;
    private int mappingRow = 1;
    private char delimiter;
    private boolean newBeanPerRecord;
    private boolean acceptPartials;
    private boolean trim;
    private boolean processEscapeSequence;
    private boolean ignoreQuotes;
    private int maximumInlineFieldsLimit;
    private boolean formatSource;
    private boolean skipCommentLines;
    private boolean skipEmptyLines;
    private boolean failOnFirstError;
    private String postProcessMethod;
    private int version;

    public CsvMetaModel(String targetClassName, String marshallerClassName, String packageName) {
        this.targetClassName = targetClassName;
        this.marshallerClassName = marshallerClassName + "CsvMarshaller";
        this.packageName = packageName;
    }

    public void registerSetMethod(String methodName) {
        String fieldName = StringUtils.uncapitalize(StringUtils.remove(methodName, "set"));
         fieldMap.computeIfAbsent(fieldName, FieldModel::of).setSetterMethod(methodName);
    }

    public void registerGetMethod(String methodName) {
        String prefix = methodName.startsWith("is")?"is":"get";
        String fieldName = StringUtils.uncapitalize(StringUtils.remove(methodName, prefix));
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setGetterMethod(methodName);
    }

    public void registerFieldType(String fieldName, String type){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setType(type);
    }

    public void setColumnName(String fieldName, String columnName){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setColumnName(columnName);
    }

    public void setDefaultFieldValue(String fieldName, String columnName){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setDefaultFieldName(columnName);
    }

    public void setOptionalField(String fieldName, boolean optionalField){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setOptionalField(optionalField);
    }

    public void setTrimField(String fieldName, boolean trimField){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setTrimField(trimField);
    }

    public void setColumnIndex(String fieldName, int columnIndex){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setColumnIndex(columnIndex);
    }

    public void setFieldConverter(String fieldName, String converterClass, String converterMethod, String convertConfiguration){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setFieldConverter(converterClass, convertConfiguration, converterMethod);
    }

    public void setLookupName(String fieldName, String lookupName){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setLookupName(lookupName);
    }

    public void setValidator(String fieldName, ValidatorConfig validatorConfig){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setFieldValidator(validatorConfig);
    }

    public void setNullWriteValue(String fieldName, String nullValue){
        fieldMap.computeIfAbsent(fieldName, FieldModel::of).setNullWriteValue(nullValue);
    }

    public void buildModel(){
        pruneInvalidFields();
        fieldMap.values().forEach(FieldModel::buildFieldModel);
    }

    private void pruneInvalidFields(){
        fieldMap.values().removeIf(FieldModel::nonMarshallField);
    }

    @Override
    public String getFqn(){
        return getPackageName() + "." + getMarshallerClassName();
    }

    @Override
    public List<CsvToFieldInfoModel> fieldInfoList(){
        return fieldMap.values().stream()
                .map(FieldModel::getCsvToFieldInfo)
                .map(CsvToFieldInfoModel.class::cast)
                .collect(Collectors.toList());
    }

    public List<FieldToCsvInfo> outputFieldInfoList(){
        return fieldMap.values().stream()
                .map(FieldModel::getFieldToCsvInfoInfo)
//                .map(CsvToFieldInfoModel.class::cast)
                .collect(Collectors.toList());
    }

    @Data(staticConstructor = "of")
    private static class FieldModel {
        private final String name;
        private String type;
        private String getterMethod;
        private String setterMethod;
        private FieldToCsvInfo fieldToCsvInfoInfo;
        private CsvToFieldInfo csvToFieldInfo;


        public boolean nonMarshallField() {
            return getterMethod == null || setterMethod == null;
        }

        public void buildFieldModel(){
            csvToFieldInfo = new CsvToFieldInfo();
            csvToFieldInfo.setSourceFieldName(name);
            csvToFieldInfo.setTarget(getterMethod, setterMethod, false, type, "target");
            fieldToCsvInfoInfo = new FieldToCsvInfo();
            fieldToCsvInfoInfo.setSourceMethod(getterMethod);
            fieldToCsvInfoInfo.setEnumField(false);
            fieldToCsvInfoInfo.setSourceType(type);
        }

        public void setColumnName(String columnName){
            csvToFieldInfo.setSourceFieldName(columnName);
        }

        public void setDefaultFieldName(String defaultValue){
            csvToFieldInfo.setDefaultValue(defaultValue);
        }

        public void setOptionalField(boolean optionalField){
            csvToFieldInfo.setMandatory(!optionalField);
        }

        public void setTrimField(boolean trimField){
            csvToFieldInfo.setTrim(trimField);
        }

        public void setColumnIndex(int columnIndex){
            csvToFieldInfo.setSourceColIndex(columnIndex);
        }

        public void setFieldConverter(String converterClass, String convertConfiguration, String converterMethod) {
            csvToFieldInfo.setConverter(converterClass, convertConfiguration, converterMethod);
            fieldToCsvInfoInfo.setConverterId(csvToFieldInfo.getConverterInstanceId());
        }

        public void setFieldValidator(ValidatorConfig validatorConfig){
            csvToFieldInfo.setValidatorConfig(validatorConfig);
        }

        public void setNullWriteValue(String nullValue){
            fieldToCsvInfoInfo.setNullValue(nullValue);
        }

        public void setLookupName(String lookupName){
            csvToFieldInfo.setLookupKey(lookupName);
        }
    }

}
