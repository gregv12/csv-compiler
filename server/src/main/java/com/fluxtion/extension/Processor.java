package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.ColumnMapping;
import com.fluxtion.extension.csvcompiler.CsvProcessingConfig;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import com.fluxtion.extension.csvcompiler.processor.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;


public class Processor {

    public static final String PACKAGE_NAME = "com.example.helloworld";
    private final CsvProcessingConfig processingConfig;
    private final Builder csvBeanClassBuilder;
    private final MethodSpec.Builder toStringBuilder;
    private final MethodSpec.Builder accessByNameBuilder;
    private String previousFieldName = null;

    public Processor(CsvProcessingConfig processingConfig) {
        this.processingConfig = processingConfig;
        csvBeanClassBuilder = TypeSpec.classBuilder(processingConfig.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(FieldAccessor.class);
        toStringBuilder = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("$T toString = $S", String.class, processingConfig.getName() + "[");
        accessByNameBuilder = MethodSpec.methodBuilder("getField")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .addAnnotation(Override.class)
                .addParameter(String.class, "fieldName")
                .beginControlFlow("switch(fieldName)")
                .returns(TypeVariableName.get("T"));
        Yaml yaml = new Yaml();
        System.out.println("myconfig:\n" + yaml.dump(processingConfig));
    }

    @SneakyThrows
    public static RowMarshaller<FieldAccessor> fromYaml(String csvProcessingConfig) {
        return new Processor(new Yaml().loadAs(csvProcessingConfig, CsvProcessingConfig.class)).load();
    }

    @SneakyThrows
    public RowMarshaller<FieldAccessor> load() throws IOException {
        addClassAnnotations();
        addConversionFunctions();
        processingConfig.getColumns().forEach((k, v) -> v.setName(k));
        processingConfig.getColumns().values().forEach(this::addFields);
        addGetField();
        addToString();
        TypeSpec beanCsvClass = csvBeanClassBuilder.build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, beanCsvClass).build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        System.out.println("compiling:\n" + stringWriter.toString());
        String fqn = PACKAGE_NAME + "." + beanCsvClass.name;
        String marshallerFqn = fqn + "CsvMarshaller";
        Object instance = Util.compileInstance(fqn, stringWriter.toString());
        Class<?> aClass = instance.getClass().getClassLoader().loadClass(marshallerFqn);
        return (RowMarshaller<FieldAccessor>) aClass.getConstructor().newInstance();
    }

    private void addConversionFunctions() {
        processingConfig.getConversionFunctions().forEach(
                (k, v) ->{
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
        toStringBuilder.addStatement("toString += $S + $L + $S", previousFieldName + ":'", previousFieldName, "'");
        toStringBuilder.addStatement("toString += $S", "]");
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
                                .addMember("columnName", "$S", columnMapping.getCsvColumnName())
                                .addMember("columnIndex", "$L", columnMapping.getCsvIndex())
                                .addMember("trimOverride", "$L", columnMapping.isTrimOverride())
                                .addMember("optionalField", "$L", columnMapping.isOptional())
                                .addMember("defaultValue", "$S", columnMapping.getDefaultValue())
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
        //toString
        if (previousFieldName != null) {
            toStringBuilder.addStatement("toString += $S + $L + $S", previousFieldName + ":'", previousFieldName, "', ");
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
        if(!StringUtils.isBlank(columnMapping.getConverterCode())){
            addedAnnotation = true;
            String conversionFunctionName = "convert_" + StringUtils.capitalize(columnMapping.getName());
            annotationBuilder.addMember("conversionMethod", "$S", conversionFunctionName);
            csvBeanClassBuilder.addMethod(MethodSpec.methodBuilder(conversionFunctionName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CharSequence.class, "input")
                    .returns(columnMapping.asTypeName())
                    .addCode(columnMapping.getConverterCode())
                    .build());
        }else if(!StringUtils.isBlank(columnMapping.getConverterFunction())){
            addedAnnotation = true;
            annotationBuilder.addMember("conversionMethod", "$S", columnMapping.getConverterFunction());
        }
        if(addedAnnotation){
            fieldBuilder.addAnnotation(annotationBuilder.build());
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
                        .addMember("fieldSeparator", "'$L'", processingConfig.getFieldSeparator())
                        .addMember("ignoreQuotes", "$L", processingConfig.isIgnoreQuotes())
                        .addMember("trim", "$L", processingConfig.isTrim())
                        .addMember("failOnFirstError", "$L", processingConfig.isFailOnFirstError())
                        .build());
    }

    @SneakyThrows
    public static TypeName asTypeName(String typeNameString){
        TypeName typeName;
        switch (typeNameString) {
            case "int":
                typeName = TypeName.INT;
                break;
            case "double":
                typeName = TypeName.DOUBLE;
                break;
            case "short":
                typeName = TypeName.SHORT;
                break;
            case "long":
                typeName = TypeName.LONG;
                break;
            case "char":
                typeName = TypeName.CHAR;
                break;
            case "float":
                typeName = TypeName.FLOAT;
                break;
            case "boolean":
                typeName = TypeName.BOOLEAN;
                break;
            default:
                typeName = TypeName.get(Class.forName(typeNameString));
        }
        return typeName;
    }
}
