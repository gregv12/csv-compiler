package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import com.fluxtion.extension.csvcompiler.annotations.Validator;
import com.fluxtion.extension.csvcompiler.converters.LibraryConverter;
import com.fluxtion.extension.csvcompiler.processor.Util;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.element.Modifier;
import java.io.Reader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


public class CsvChecker {

    public static final String PACKAGE_NAME = "com.fluxtion.extension.csvcompilere.generated";
    private static Map<String, String> classShortNameMap = Map.of(
            "String", String.class.getCanonicalName(),
            "string", String.class.getCanonicalName(),
            "dateTime", LocalDateTime.class.getCanonicalName(),
            "DateTime", LocalDateTime.class.getCanonicalName(),
            "time", LocalTime.class.getCanonicalName(),
            "Time", LocalTime.class.getCanonicalName(),
            "date", LocalDate.class.getCanonicalName(),
            "Date", LocalDate.class.getCanonicalName()
    );
    private final CsvProcessingConfig processingConfig;
    private final Builder csvBeanClassBuilder;
    private final MethodSpec.Builder toStringBuilder;
    private final MethodSpec.Builder accessByNameBuilder;
    private String previousFieldName = null;

    public CsvChecker(CsvProcessingConfig processingConfig) {
        this.processingConfig = processingConfig;
        processingConfig.getDerivedColumns().forEach((k, v) -> {
            v.setDerived(true);
            v.setName(k);
        });
        processingConfig.getColumns().putAll(processingConfig.getDerivedColumns());
        csvBeanClassBuilder = TypeSpec.classBuilder(processingConfig.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(FieldAccessor.class);
        toStringBuilder = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("$T toString = $S", String.class, processingConfig.getName() + ": {");
        accessByNameBuilder = MethodSpec.methodBuilder("getField")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .addAnnotation(Override.class)
                .addParameter(String.class, "fieldName")
                .beginControlFlow("switch(fieldName)")
                .returns(TypeVariableName.get("T"));
        if(processingConfig.isDumpYaml()){
            Yaml yaml = new Yaml();
            System.out.println("myconfig:\n" + yaml.dump(processingConfig));
        }
    }

    @SneakyThrows
    public static RowMarshaller<FieldAccessor> fromYaml(String csvProcessingConfig) {
        return new CsvChecker(new Yaml().loadAs(csvProcessingConfig, CsvProcessingConfig.class)).load();
    }

    @SneakyThrows
    public static RowMarshaller<FieldAccessor> fromYaml(Reader reader) {
        return new CsvChecker(new Yaml().loadAs(reader, CsvProcessingConfig.class)).load();
    }

    @SneakyThrows
    public static TypeName asTypeName(String typeNameString) {
        if (StringUtils.isBlank(typeNameString)) {
            return ClassName.get(CharSequence.class);
        }
        TypeName typeName;
        switch (typeNameString) {
            case "int":
                typeName = TypeName.INT;
                break;
            case "int[]":
                typeName = ArrayTypeName.of(TypeName.INT);
                break;
            case "List<int>":
            case "List<Integer>":
                typeName = ParameterizedTypeName.get(List.class, Integer.class);
                break;
            case "double":
                typeName = TypeName.DOUBLE;
                break;
            case "double[]":
                typeName = ArrayTypeName.of(TypeName.INT);
                break;
            case "List<double>":
            case "List<Double>":
                typeName = ParameterizedTypeName.get(List.class, Double.class);
                break;
            case "short":
                typeName = TypeName.SHORT;
                break;
            case "short[]":
                typeName = ArrayTypeName.of(TypeName.SHORT);
                break;
            case "List<short>":
            case "List<Short>":
                typeName = ParameterizedTypeName.get(List.class, Short.class);
                break;
            case "long":
                typeName = TypeName.LONG;
                break;
            case "long[]":
                typeName = ArrayTypeName.of(TypeName.LONG);
                break;
            case "List<long>":
            case "List<Long>":
                typeName = ParameterizedTypeName.get(List.class, Long.class);
                break;
            case "char":
                typeName = TypeName.CHAR;
                break;
            case "float":
                typeName = TypeName.FLOAT;
                break;
            case "float[]":
                typeName = ArrayTypeName.of(TypeName.FLOAT);
                break;
            case "List<float>":
            case "List<Float>":
                typeName = ParameterizedTypeName.get(List.class, Float.class);
                break;
            case "boolean":
                typeName = TypeName.BOOLEAN;
                break;
            default:
                String lookupName = classShortNameMap.getOrDefault(typeNameString, typeNameString);
                if(typeNameString.contains("[]")){
                    typeName = ArrayTypeName.get(Class.forName(lookupName));
                }else{
                    typeName = TypeName.get(Class.forName(lookupName));
                }
        }
        return typeName;
    }

    @SneakyThrows
    public RowMarshaller<FieldAccessor> load() {
        addClassAnnotations();
        addConversionFunctions();
        addValidationFunctions();
        processingConfig.getColumns().forEach((k, v) -> v.setName(k));
        processingConfig.getColumns().values().forEach(this::addFields);
        addGetField();
        addToString();
        TypeSpec beanCsvClass = csvBeanClassBuilder.build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, beanCsvClass).build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String fqn = PACKAGE_NAME + "." + beanCsvClass.name;
        String marshallerFqn = fqn + "CsvMarshaller";
        if(processingConfig.isDumpGeneratedJava()){
            System.out.println("compiling:\n" + stringWriter.toString());
        }
        Object instance = Util.compileInstance(fqn, stringWriter.toString());
        Class<?> aClass = instance.getClass().getClassLoader().loadClass(marshallerFqn);
        RowMarshaller<FieldAccessor> rowMarshaller = (RowMarshaller<FieldAccessor>) aClass.getConstructor().newInstance();
        addLookupTables(rowMarshaller);

        return rowMarshaller;
    }

    private void addLookupTables(RowMarshaller<FieldAccessor> rowMarshaller) {
        Map<String, Map<String, String>> lookupTables = processingConfig.getLookupTables();
        lookupTables.forEach((k, t) -> {
            try {
                rowMarshaller.addLookup(k, l -> {
                    Object value = t.get(l);
                    if (value == null) {
                        value = t.getOrDefault("default", "");
                    }
                    return value.toString();
                });
            } catch (Exception e) {
                System.out.println("problem adding the lookup:" + k + " error:" + e);
            }
        });
    }

    private void addValidationFunctions() {
        final TypeName validationLogType = ParameterizedTypeName.get(
                ClassName.get(BiConsumer.class),
                ClassName.get(String.class), ClassName.get(Boolean.class));
        processingConfig.getValidationFunctions().forEach(
                (k, v) -> {
                    csvBeanClassBuilder.addMethod(MethodSpec.methodBuilder(k)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(validationLogType, "validationLog")
                            .returns(TypeName.BOOLEAN)
                            .addCode(v.getCode())
                            .build());
                }
        );
    }

    private void addConversionFunctions() {
        processingConfig.getConversionFunctions().forEach(
                (k, v) -> {
                    csvBeanClassBuilder.addMethod(MethodSpec.methodBuilder(k)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(CharSequence.class, "input")
                            .returns(asTypeName(v.getConvertsTo()))
                            .addCode(v.getCode())
                            .build());
                }
        );
    }

    private void addGetField() {
        accessByNameBuilder.addCode("default:\n")
                .addStatement("$>break")
                .endControlFlow()
                .addStatement("return null");
        csvBeanClassBuilder.addMethod(accessByNameBuilder.build());
    }

    private void addToString() {
        toStringBuilder.addStatement("toString += $S + $L", previousFieldName + ": ", previousFieldName);
        toStringBuilder.addStatement("toString += $S", "}");
        toStringBuilder.addStatement("return toString");
        csvBeanClassBuilder.addMethod(toStringBuilder.build());
    }

    @lombok.SneakyThrows
    private void addFields(ColumnMapping columnMapping) {
        TypeName typeName = columnMapping.asTypeName();
        String fieldName = columnMapping.getName();
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(
                        AnnotationSpec.builder(com.fluxtion.extension.csvcompiler.annotations.ColumnMapping.class)
                                .addMember("columnName", "$S", columnMapping.getSourceColumnName())
                                .addMember("columnIndex", "$L", columnMapping.getSourceColumnIndex())
                                .addMember("trimOverride", "$L", columnMapping.isTrimOverride())
                                .addMember("escapeOutput", "$L", columnMapping.isEscapeOutput())
                                .addMember("optionalField", "$L", columnMapping.isOptional())
                                .addMember("defaultValue", "$S", columnMapping.getDefaultValue())
                                .addMember("outputField", "$L", columnMapping.isOutputField())
                                .build()
                );


        String get = typeName == TypeName.BOOLEAN ? "is" : "get";
        MethodSpec getter = MethodSpec.methodBuilder(get + StringUtils.capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addStatement("return " + fieldName)
                .build();
        csvBeanClassBuilder.addMethod(getter);

        MethodSpec setter = MethodSpec.methodBuilder("set" + StringUtils.capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, fieldName)
                .addStatement("this." + fieldName + " = " + fieldName)
                .build();
        csvBeanClassBuilder.addMethod(setter);
        //converter methods
        addConverterMethods(fieldBuilder, columnMapping);
        //validation methods
        addValidationMethod(fieldBuilder, columnMapping);
        //toString
        if (previousFieldName != null) {
            toStringBuilder.addStatement("toString += $S + $L + $S", previousFieldName + ": ", previousFieldName, ", ");
        }
        //field accessor
        accessByNameBuilder.addCode("case $S:\n", fieldName)
                .addStatement("$>return (T)(Object)$L$<", fieldName);
        previousFieldName = fieldName;
        //build and add
        csvBeanClassBuilder.addField(fieldBuilder.build());

    }

    private void addConverterMethods(FieldSpec.Builder fieldBuilder, ColumnMapping columnMapping) {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(DataMapping.class);
        boolean addedAnnotation = false;
        if (!StringUtils.isBlank(columnMapping.getConverterCode())) {
            addedAnnotation = true;
            String conversionFunctionName = "convert_" + StringUtils.capitalize(columnMapping.getName());
            annotationBuilder.addMember("conversionMethod", "$S", conversionFunctionName);
            csvBeanClassBuilder.addMethod(MethodSpec.methodBuilder(conversionFunctionName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CharSequence.class, "input")
                    .returns(columnMapping.asTypeName())
                    .addCode(columnMapping.getConverterCode())
                    .build());
        } else if (!StringUtils.isBlank(columnMapping.getConverterFunction())) {
            addedAnnotation = true;
            annotationBuilder.addMember("conversionMethod", "$S", columnMapping.getConverterFunction());
        } else if(!StringUtils.isBlank(columnMapping.getConverter())){
            addedAnnotation = true;
            annotationBuilder.addMember("converter", "$T.class", LibraryConverter.getConverter(columnMapping.getConverter()));
            annotationBuilder.addMember("configuration", "$S", columnMapping.getConverterConfiguration());
        }
        if (columnMapping.isDerived()) {
            addedAnnotation = true;
            annotationBuilder.addMember("derivedColumn", "$L", true);
        }
        if (!StringUtils.isBlank(columnMapping.getLookupTable())) {
            addedAnnotation = true;
            annotationBuilder.addMember("lookupName", "$S", columnMapping.getLookupTable());
        }
        //lookupName
        if (addedAnnotation) {
            fieldBuilder.addAnnotation(annotationBuilder.build());
        }
    }

    private void addValidationMethod(FieldSpec.Builder fieldBuilder, ColumnMapping columnMapping) {
        if (!StringUtils.isBlank(columnMapping.getValidationFunction())) {
            fieldBuilder.addAnnotation(
                    AnnotationSpec.builder(Validator.class)
                            .addMember("validationMethod", "$S", columnMapping.getValidationFunction())
                            .build());
        }
    }

    private void addClassAnnotations() {
        csvBeanClassBuilder.addAnnotation(
                AnnotationSpec.builder(CsvMarshaller.class)
                        .addMember("acceptPartials", "$L", processingConfig.isAcceptPartials())
                        .addMember("mappingRow", "$L", processingConfig.getMappingRow())
                        .addMember("headerLines", "$L", processingConfig.getHeaderLines())
                        .addMember("skipCommentLines", "$L", processingConfig.isSkipCommentLines())
                        .addMember("processEscapeSequences", "$L", processingConfig.isProcessEscapeSequences())
                        .addMember("skipEmptyLines", "$L", processingConfig.isSkipEmptyLines())
                        .addMember("fieldSeparator", "'$L'", processingConfig.getFieldSeparator() == '\t' ? "\\t" : processingConfig.getFieldSeparator())
                        .addMember("ignoreQuotes", "$L", processingConfig.isIgnoreQuotes())
                        .addMember("trim", "$L", processingConfig.isTrim())
                        .addMember("failOnFirstError", "$L", processingConfig.isFailOnFirstError())
                        .build());
    }
}
