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
import com.fluxtion.extension.csvcompiler.processor.model.CodeGenerator;
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
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
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
            CodeGenerator codeGenerator = new CodeGenerator(writer, model);
            codeGenerator.writeMarshaller();
        }
    }

    private CsvMetaModel modelCsvMarshaller(TypeElement typeElement) {
        PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(typeElement);
        final String packageName = packageOf.getQualifiedName().toString();
        final String targetType = StringUtils.remove(typeElement.getQualifiedName().toString(), packageName + ".");
        final String className = targetType.replace(".", "_");//typeElement.getSimpleName().toString();
        CsvMetaModel csvMetaModel = new CsvMetaModel(targetType, className, packageName);
        registerGetters(csvMetaModel, typeElement);
        registerSetters(csvMetaModel, typeElement);
        potProcessMethod(csvMetaModel, typeElement);
        setMarshallerOptions(csvMetaModel, typeElement);
        csvMetaModel.buildModel();
        //apply field customisations
        processingEnv.getElementUtils().getAllMembers(typeElement).forEach(e -> {
            ColumnMapping columnMapping = e.getAnnotation(ColumnMapping.class);
            Name variableName = e.getSimpleName();
            if (columnMapping != null) {
                if (!columnMapping.columnName().isBlank()) {
                    csvMetaModel.setColumnName(variableName.toString(), columnMapping.columnName());
                }
                if (!columnMapping.defaultValue().isBlank()) {
                    csvMetaModel.setDefaultFieldValue(variableName.toString(), columnMapping.defaultValue());
                }
                if (columnMapping.optionalField()) {
                    csvMetaModel.setOptionalField(variableName.toString(), true);
                }
                if (columnMapping.columnIndex() > -1) {
                    csvMetaModel.setColumnIndex(variableName.toString(), columnMapping.columnIndex());
                }
                csvMetaModel.setTrimField(variableName.toString(), columnMapping.trimOverride());
            }

            DataMapping dataMapping = e.getAnnotation(DataMapping.class);
            if (dataMapping != null){
                    List<? extends TypeMirror> types = getTypeMirrorFromAnnotationValue(dataMapping::converter);
                    TypeMirror typeMirror = types.get(0);
                    Types TypeUtils = this.processingEnv.getTypeUtils();
                    TypeElement typeElement1 = (TypeElement) TypeUtils.asElement(typeMirror);
                    String fqnConverter = typeElement1.getQualifiedName().toString();
                    if(!fqnConverter.equalsIgnoreCase(FieldConverter.NULL.class.getCanonicalName())){
                        String format = dataMapping.configuration();
                        csvMetaModel.setFieldConverter(variableName.toString(), typeElement1.getQualifiedName().toString(), format);
                    }
                    if(!dataMapping.lookupName().isBlank()){
                        csvMetaModel.setLookupName(variableName.toString(), dataMapping.lookupName());
                    }
            }

            Validator validator = e.getAnnotation((Validator.class));
            if(validator != null && !validator.value().isBlank()){
                csvMetaModel.setValidator(variableName.toString(), ValidatorConfig.fromAnnotation(validator));
            }

        });

        return csvMetaModel;
    }


    @FunctionalInterface
    public interface GetClassValue {
        void execute() throws MirroredTypeException, MirroredTypesException;
    }

    public static List<? extends TypeMirror> getTypeMirrorFromAnnotationValue(GetClassValue c) {
        try {
            c.execute();
        }
        catch(MirroredTypesException ex) {
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
        MoreElements.getLocalAndInheritedMethods(
                        typeElement, processingEnv.getTypeUtils(), processingEnv.getElementUtils())
                .stream()
                .filter(el -> MoreElements.hasModifiers(Modifier.PUBLIC).apply(el))
                .filter(el -> el.getParameters().size() == 0)
                .filter(el -> el.getReturnType().getKind() != TypeKind.NULL)
                .filter(el -> el.getSimpleName().toString().startsWith("get") || el.getSimpleName().toString().startsWith("is"))
                .map(el -> {
                    String type = el.getReturnType().toString();
                    Element element = processingEnv.getTypeUtils().asElement(el.getReturnType());
                    if (element != null) {
                        type = element.getSimpleName().toString();
                    }
                    String prefix = type.equalsIgnoreCase("boolean") ? "is" : "get";
                    String fieldName = StringUtils.uncapitalize(StringUtils.remove(el.getSimpleName().toString(), prefix));
                    csvMetaModel.registerFieldType(fieldName, type);
                    return el.getSimpleName().toString();
                })
                .forEach(csvMetaModel::registerGetMethod);
    }

    private void registerSetters(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        MoreElements.getLocalAndInheritedMethods(
                        typeElement, processingEnv.getTypeUtils(), processingEnv.getElementUtils())
                .stream()
                .filter(el -> MoreElements.hasModifiers(Modifier.PUBLIC).apply(el))
                .filter(el -> el.getParameters().size() == 1)
                .map(el -> el.getSimpleName().toString())
                .filter(name -> name.startsWith("set"))
                .forEach(csvMetaModel::registerSetMethod);
    }

    private void setMarshallerOptions(CsvMetaModel csvMetaModel, TypeElement typeElement) {
        CsvMarshaller annotation = typeElement.getAnnotation(CsvMarshaller.class);
        if(annotation.noHeader()){
            csvMetaModel.setMappingRow(0);
            csvMetaModel.setHeaderLines(0);
        }else{
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
    }

}

