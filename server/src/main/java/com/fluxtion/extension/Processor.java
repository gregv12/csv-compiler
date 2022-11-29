package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.ColumnMapping;
import com.fluxtion.extension.csvcompiler.CsvProcessingConfig;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.processor.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;


public class Processor {

    public static final String PACKAGE_NAME = "com.example.helloworld";
    private final CsvProcessingConfig processingConfig;
    private Builder csvBeanClassBuilder;

    public Processor(CsvProcessingConfig processingConfig) {
        this.processingConfig = processingConfig;
        Yaml yaml = new Yaml();
        System.out.println("myconfig:\n" + yaml.dump(processingConfig));
    }

    @SneakyThrows
    public void load() throws IOException {
        csvBeanClassBuilder = TypeSpec.classBuilder(processingConfig.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        addClassAnnotations();
        processingConfig.getColumnMap().values().forEach(this::addFields);

        TypeSpec beanCsvClass = csvBeanClassBuilder
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, beanCsvClass)
                .build();
        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        System.out.println("compiling:\n" + stringWriter.toString());
        String fqn = PACKAGE_NAME + "." + beanCsvClass.name;
        String marshallerFqn = fqn + "CsvMarshaller";
        Object instance = Util.compileInstance(fqn, stringWriter.toString());
        Class<?> aClass = instance.getClass().getClassLoader().loadClass(marshallerFqn);
        RowMarshaller<?> rowMarshaller = (RowMarshaller<?>) aClass.getConstructor().newInstance();
        StringBuilder sb = new StringBuilder();
        rowMarshaller.writeHeaders(sb);
    }

    @lombok.SneakyThrows
    private void addFields(ColumnMapping columnMapping) {
        TypeName typeName;
        switch (columnMapping.getType()) {
            case "int":
                typeName = TypeName.INT;
                break;
            case "boolean":
                typeName = TypeName.BOOLEAN;
                break;
            default:
                typeName = TypeName.get(Class.forName(columnMapping.getType()));
        }
        String fieldName = columnMapping.getName();
        FieldSpec field = FieldSpec.builder(typeName, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(
                        AnnotationSpec.builder(com.fluxtion.extension.csvcompiler.annotations.ColumnMapping.class)
                                .addMember("columnName", "$S", columnMapping.getCsvColumnName())
                                .addMember("columnIndex", "$L", columnMapping.getCsvIndex())
                                .addMember("trimOverride", "$L", columnMapping.isTrimOverride())
                                .addMember("optionalField", "$L", columnMapping.isOptional())
                                .addMember("defaultValue", "$S", columnMapping.getDefaultValue())
                                .build()
                )
                .build();
        csvBeanClassBuilder.addField(field);


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
}
