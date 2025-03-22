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

package com.fluxtion.extension.csvcompiler.processor;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.fluxtion.extension.csvcompiler.annotations.*;
import com.fluxtion.extension.csvcompiler.converters.*;
import com.fluxtion.extension.csvcompiler.processor.model.CodeGenerator;
import com.fluxtion.extension.csvcompiler.processor.model.CodeGeneratorNoBufferCopy;
import com.fluxtion.extension.csvcompiler.processor.model.CsvMetaModel;
import com.fluxtion.extension.csvcompiler.processor.model.ValidatorConfig;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class CsvMarshallerGenerator implements Processor {
    boolean processed;
    private ProcessingEnvironment processingEnv;

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("com.fluxtion.extension.csvcompiler.*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(CsvMarshaller.class).stream()
                .map(TypeElement.class::cast)
                .forEach(this::generateMarshaller);
        this.processed = true;
        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptyList();
    }

    @SneakyThrows
    private void generateMarshaller(TypeElement element) {
        final CsvMetaModel model = modelCsvMarshaller(element);
        final JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(model.getFqn());
        try (Writer writer = fileObject.openWriter()) {
            if (model.getVersion() == 1) {
                CodeGenerator codeGenerator = new CodeGenerator(writer, model);
                codeGenerator.writeMarshaller();
            } else {
                CodeGeneratorNoBufferCopy codeGenerator = new CodeGeneratorNoBufferCopy(writer, model);
                codeGenerator.writeMarshaller();
            }
        }
    }

    private CsvMetaModel modelCsvMarshaller(TypeElement typeElement) {
        PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(typeElement);
        final String packageName = packageOf.getQualifiedName().toString();
        final String targetType = StringUtils.remove(typeElement.getQualifiedName().toString(), packageName + ".");
        final String className = targetType.replace(".", "_");//typeElement.getSimpleName().toString();
        CsvMetaModel csvMetaModel = new CsvMetaModel(targetType, className, packageName);
        setMarshallerOptions(csvMetaModel, typeElement);
        registerGetters(csvMetaModel, typeElement);
        registerSetters(csvMetaModel, typeElement);
        potProcessMethod(csvMetaModel, typeElement);
        csvMetaModel.buildModel();
        //apply field customisations
        processingEnv.getElementUtils().getAllMembers(typeElement).forEach(e -> {

            boolean ignoreMember = true;
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case RESOURCE_VARIABLE: {
                    ignoreMember = false;
                }
            }
            if (ignoreMember) {
                return;
            }

            ColumnMapping columnMapping = e.getAnnotation(ColumnMapping.class);
            Name variableName = e.getSimpleName();
            checkArrayConversion(e, csvMetaModel);
            checkList(e, csvMetaModel);
            checkEnum(e, csvMetaModel);
            if (columnMapping != null) {
                validateFieldName(csvMetaModel, variableName.toString());
                if (!StringUtils.isBlank(columnMapping.columnName())) {
                    csvMetaModel.setInputColumnName(variableName.toString(), columnMapping.columnName());
                }
                if (!StringUtils.isBlank(columnMapping.defaultValue())) {
                    csvMetaModel.setDefaultFieldValue(variableName.toString(), columnMapping.defaultValue());
                }
                if (columnMapping.optionalField()) {
                    csvMetaModel.setOptionalField(variableName.toString(), true);
                }
                if (columnMapping.columnIndex() > -1) {
                    csvMetaModel.setColumnIndex(variableName.toString(), columnMapping.columnIndex());
                } else {
                    if (!csvMetaModel.isMappingRowPresent()) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "index column required if no mapping row is present",
                                e
                        );
                    }
                }
                csvMetaModel.setTrimField(variableName.toString(), columnMapping.trimOverride());
                csvMetaModel.setEscapeFieldOutput(variableName.toString(), columnMapping.escapeOutput());
                csvMetaModel.setWriteFieldToOutput(variableName.toString(), columnMapping.outputField());
            }

            DataMapping dataMapping = e.getAnnotation(DataMapping.class);
            if (dataMapping != null) {
                validateFieldName(csvMetaModel, variableName.toString());
                List<? extends TypeMirror> types = getTypeMirrorFromAnnotationValue(dataMapping::converter);
                TypeMirror typeMirror = types.get(0);
                Types TypeUtils = this.processingEnv.getTypeUtils();
                TypeElement typeElement1 = (TypeElement) TypeUtils.asElement(typeMirror);
                String fqnConverter = typeElement1.getQualifiedName().toString();
                if (!fqnConverter.equalsIgnoreCase(FieldConverter.NULL.class.getCanonicalName())) {
                    String format = dataMapping.configuration();
                    String converterMethod = dataMapping.conversionMethod();
                    csvMetaModel.setFieldConverter(variableName.toString(), typeElement1.getQualifiedName().toString(), converterMethod, format);
                } else if (!StringUtils.isBlank(dataMapping.conversionMethod())) {
                    //local converter method
                    csvMetaModel.setFieldConverter(variableName.toString(), null, dataMapping.conversionMethod(), "");
                }
                if (!StringUtils.isBlank(dataMapping.lookupName())) {
                    csvMetaModel.setLookupName(variableName.toString(), dataMapping.lookupName());
                }
                if (dataMapping.checkNullOnWrite()) {
                    csvMetaModel.setNullWriteValue(variableName.toString(), dataMapping.nullWriteValue());
                } else {
                    csvMetaModel.setNullWriteValue(variableName.toString(), null);
                }
                csvMetaModel.setDerivedFlag(variableName.toString(), dataMapping.derivedColumn());
            }

            Validator validator = e.getAnnotation((Validator.class));
            if (validator != null && (!StringUtils.isBlank(validator.validationLambda()) || !StringUtils.isBlank(validator.validationMethod()))) {
                validateFieldName(csvMetaModel, variableName.toString());
                csvMetaModel.setValidator(variableName.toString(), ValidatorConfig.fromAnnotation(validator, targetType));
            }
        });

        return csvMetaModel;
    }

    private void checkEnum(Element element, CsvMetaModel csvMetaModel) {
        TypeMirror elementType = element.asType();
        Element element1 = processingEnv.getTypeUtils().asElement(elementType);

        if (element1 != null && element1.getKind() == ElementKind.ENUM) {
            Name variableName = element.getSimpleName();
            csvMetaModel.setEnumField(variableName.toString());
        }
    }

    private void checkArrayConversion(Element e, CsvMetaModel csvMetaModel) {
        if (e.asType().getKind() == TypeKind.ARRAY) {
            Name variableName = e.getSimpleName();
            String fqn = e.asType().toString();
            boolean previousEscapeFlag = csvMetaModel.isProcessEscapeSequence();
            csvMetaModel.setProcessEscapeSequence(true);
//            processingEnv.getMessager().printMessage(Kind.WARNING, "variableName:" + variableName + "array type:" + fqn);
            switch (fqn) {
                case "byte[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayByteConverter.class.getCanonicalName());
                    break;
                case "double[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayDoubleConverter.class.getCanonicalName());
                    break;
                case "float[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayFloatConverter.class.getCanonicalName());
                    break;
                case "long[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayLongConverter.class.getCanonicalName());
                    break;
                case "int[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayIntConverter.class.getCanonicalName());
                    break;
                case "short[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayShortConverter.class.getCanonicalName());
                    break;
                case "java.lang.String[]":
                    csvMetaModel.setFieldConverter(variableName.toString(), ArrayStringConverter.class.getCanonicalName());
                    break;
                default:
                    csvMetaModel.setProcessEscapeSequence(previousEscapeFlag);
            }
        }
    }

    private void checkList(Element e, CsvMetaModel csvMetaModel) {
        if (e.asType().getKind() == TypeKind.DECLARED && e.getKind() == ElementKind.FIELD) {
            Name variableName = e.getSimpleName();
            String fqn = e.asType().toString();
            boolean previousEscapeFlag = csvMetaModel.isProcessEscapeSequence();
            csvMetaModel.setProcessEscapeSequence(true);
//            processingEnv.getMessager().printMessage(Kind.WARNING, "variableName:" + variableName + "list type:" + fqn);
            switch (fqn) {
                case "java.util.List<java.lang.Byte>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListByteConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.Double>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListDoubleConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.Float>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListFloatConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.Long>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListLongConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.Integer>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListIntegerConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.Short>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListShortConverter.class.getCanonicalName());
                    break;
                case "java.util.List<java.lang.String>":
                    csvMetaModel.setFieldConverter(variableName.toString(), ListStringConverter.class.getCanonicalName());
                    break;
                default:
                    csvMetaModel.setProcessEscapeSequence(previousEscapeFlag);
            }
        }
    }

    @SneakyThrows
    private void validateFieldName(CsvMetaModel csvMetaModel, String fieldName) {
        if (csvMetaModel.getFieldMap().get(fieldName) == null) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "missing getter/setter for field: " + fieldName);
            throw new NoSuchMethodException(String.format("get%1$s set%1$s", StringUtils.capitalize(fieldName)));
        }
    }

    @FunctionalInterface
    public interface GetClassValue {
        void execute() throws MirroredTypeException, MirroredTypesException;
    }

    public static List<? extends TypeMirror> getTypeMirrorFromAnnotationValue(GetClassValue c) {
        try {
            c.execute();
        } catch (MirroredTypesException ex) {
            return ex.getTypeMirrors();
        }
        return null;
    }

    private void potProcessMethod(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        MoreElements.getLocalAndInheritedMethods(
                        typeElement, processingEnv.getTypeUtils(), processingEnv.getElementUtils())
                .stream()
                .filter(el -> el.getParameters().size() == 0)
                .filter(el -> MoreElements.isAnnotationPresent(el, PostProcessMethod.class))
                .forEach(el -> csvMetaModel.setPostProcessMethod(el.getSimpleName().toString()));
    }

    private void registerGetters(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        CsvMarshaller annotation = typeElement.getAnnotation(CsvMarshaller.class);
        boolean lombokPresent = false;
        boolean fluent = annotation.fluent();
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            DeclaredType dt = annotationMirror.getAnnotationType();
            String fqn = dt.asElement().toString();
            lombokPresent |= fqn.contains("lombok.Data") | fqn.contains("lombok.Getter");
        }
        if (lombokPresent) {
//            processingEnv.getMessager().printMessage(Kind.NOTE, "lombokPresent:" + lombokPresent + " type:" + typeElement.toString());
        }
        if (annotation.requireGetSetInSourceCode() & !lombokPresent & !fluent) {
            MoreElements.getLocalAndInheritedMethods(
                            typeElement, processingEnv.getTypeUtils(), processingEnv.getElementUtils())
                    .stream()
                    .filter(el -> MoreElements.hasModifiers(Modifier.PUBLIC).apply(el))
                    .filter(el -> el.getParameters().size() == 0)
                    .filter(el -> el.getReturnType().getKind() != TypeKind.NULL)
                    .filter(el -> fluent || el.getSimpleName().toString().startsWith("get") || el.getSimpleName().toString().startsWith("is"))
                    .map(el -> {
                        String type = el.getReturnType().toString();
                        Element element = processingEnv.getTypeUtils().asElement(el.getReturnType());
                        if (element != null) {
                            type = element.getSimpleName().toString();
                        }
                        String prefix = type.equalsIgnoreCase("boolean") ? "is" : "get";
                        String getMethodName = el.getSimpleName().toString();
                        String fieldName = StringUtils.uncapitalize(StringUtils.removeStart(getMethodName, prefix));
                        csvMetaModel.registerFieldType(fieldName, type);
                        return getMethodName;
                    })
                    .forEach(csvMetaModel::registerGetMethod);
        } else {
            typeElement
                    .getEnclosedElements().stream()
                    .filter(e -> e.getKind().isField())
                    .forEach(e -> {
                        String type = e.asType().toString();
                        Element element = processingEnv.getTypeUtils().asElement(e.asType());
                        if (element != null) {
                            type = element.getSimpleName().toString();
                        }
                        String fieldName = e.getSimpleName().toString();
                        String prefix = type.equalsIgnoreCase("boolean") ? "is" : "get";
                        String methodName = fluent ? fieldName : prefix + StringUtils.capitalize(fieldName);
                        csvMetaModel.registerFieldType(fieldName, type);
                        csvMetaModel.registerGetMethod(methodName);
                    });
        }
    }

    private void registerSetters(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        CsvMarshaller annotation = typeElement.getAnnotation(CsvMarshaller.class);
        boolean lombokPresent = false;
        boolean fluent = annotation.fluent();
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            DeclaredType dt = annotationMirror.getAnnotationType();
            String fqn = dt.asElement().toString();
            lombokPresent |= fqn.contains("lombok.Data") | fqn.contains("lombok.Setter");
        }
        if (lombokPresent) {
//            processingEnv.getMessager().printMessage(Kind.NOTE, "lombokPresent:" + lombokPresent + " type:" + typeElement.toString());
        }
        if (annotation.requireGetSetInSourceCode() & !lombokPresent & !fluent) {
            MoreElements.getLocalAndInheritedMethods(
                            typeElement, processingEnv.getTypeUtils(), processingEnv.getElementUtils())
                    .stream()
                    .filter(el -> MoreElements.hasModifiers(Modifier.PUBLIC).apply(el))
                    .filter(el -> el.getParameters().size() == 1)
                    .map(el -> el.getSimpleName().toString())
                    .filter(name -> name.startsWith("set"))
                    .forEach(csvMetaModel::registerSetMethod);
        } else {
            typeElement
                    .getEnclosedElements().stream()
                    .filter(e -> e.getKind().isField())
                    .forEach(e -> {
                        String fieldName = e.getSimpleName().toString();
                        String prefix = "set";
                        String methodName = fluent ? fieldName : prefix + StringUtils.capitalize(fieldName);
                        csvMetaModel.registerSetMethod(methodName);
                    });
        }
    }

    private void setMarshallerOptions(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        CsvMarshaller annotation = typeElement.getAnnotation(CsvMarshaller.class);
        if (annotation.noHeader()) {
            csvMetaModel.setMappingRow(0);
            csvMetaModel.setHeaderLines(0);
        } else {
            csvMetaModel.setMappingRow(annotation.mappingRow());
            csvMetaModel.setHeaderLines(Math.max(annotation.mappingRow(), annotation.headerLines()));
        }
        csvMetaModel.setProcessEscapeSequence(annotation.processEscapeSequences());
        csvMetaModel.setIgnoreQuotes(annotation.ignoreQuotes());
        csvMetaModel.setFormatSource(annotation.formatSource());
        csvMetaModel.setFailOnFirstError(annotation.failOnFirstError());
        csvMetaModel.setSkipCommentLines(annotation.skipCommentLines());
        csvMetaModel.setSkipEmptyLines(annotation.skipEmptyLines());
        csvMetaModel.setDelimiter(annotation.fieldSeparator());
        csvMetaModel.setNewBeanPerRecord(annotation.newBeanPerRecord());
        csvMetaModel.setAcceptPartials(annotation.acceptPartials());
        csvMetaModel.setTrim(annotation.trim());
        csvMetaModel.setVersion(annotation.versionNumber());
        csvMetaModel.setMaximumInlineFieldsLimit(annotation.loopAssignmentLimit());
    }

}

