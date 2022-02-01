package com.fluxtion.extension.csvcompiler.processor.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All the meta information required to build a csv parser and csv printer for a bean style class.
 */
@Data
public class CsvMetaModel implements CodeGeneratorModel {

    private final ImportMap importMap = ImportMap.newMap();
    private final Map<String, FieldModel> fieldMap = new HashMap<>();
    private final String marshallerClassName;
    private final String targetClassName;
    private final String packageName;
    private int headerLines = 0;
    private int mappingRow = 1;
    private char ignoreCharacter;
    private char delimiter;
    private boolean newBeanPerRecord;
    private boolean acceptPartials;
    private boolean trim;
    private boolean processEscapeSequence;
    private boolean ignoreQuotes;
    private boolean formatSource;
    private boolean skipCommentLines;
    private boolean skipEmptyLines;
    private boolean asciiOnlyHeader;
    private boolean failOnFirstError;

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
            csvToFieldInfo.setTarget(setterMethod, false, type, "target");
        }

    }

}
