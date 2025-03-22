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

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CodeGeneratorNoBufferCopy {

    private static final String CODE_TEMPLATE_DECLARATIONS = "package %1$s;\n" +
            "//VERSION 2 GENERATION - NO BUFFER COPY\n" +
            "%2$s\n" +
            "import static %1$s.%4$s.*;\n" +
            "\n" +
            "@AutoService(RowMarshaller.class)\n" +
            "public final class %3$s extends BaseMarshallerNoBufferCopy<%4$s> {\n" +
            "\n" +
            "%5$s\n" +
            "\n" +
            "    public %3$s() {\n" +
            "        super(%7$s);" +
            "        %8$s" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public Class<%4$s> targetClass() {\n" +
            "        return %4$s.class;\n" +
            "    }\n" +
            "\n" +
            "%6$s\n" +
            "\n" +
            "}\n";
    private final Writer writer;
    private final CodeGeneratorModel codeGeneratorModel;

    public CodeGeneratorNoBufferCopy(Writer writer, CodeGeneratorModel codeGeneratorModel) {
        this.writer = writer;
        this.codeGeneratorModel = codeGeneratorModel;
    }

    public static String buildLoopAssignments(CodeGeneratorModel codeGeneratorModel) {
        String assignment = "";
        if (codeGeneratorModel.isLoopAssignment()) {
            assignment += "\n        assignmentOperations = ";
            assignment += fieldAssignment(codeGeneratorModel, true);
        }
        assignment += "\n";
        return assignment;
    }

    private static String buildDeclarations(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.isHeaderPresent()) {
            options += "    private static final int HEADER_ROWS = " + codeGeneratorModel.getHeaderLines() + ";\n";
        }
        if (codeGeneratorModel.isMappingRowPresent()) {
            options += "    private static final int MAPPING_ROW = " + codeGeneratorModel.getMappingRow() + ";\n";
        }
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "    private boolean escaping = false;\n";
            options += "    private boolean prevIsQuote = false;\n";
        }
        if (codeGeneratorModel.isLoopAssignment()) {
            options += "    private final Runnable[] assignmentOperations;\n";
        }
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(CsvToFieldInfoModel::getTargetSetMethodName)
                        .map(s -> "    private final CharSequenceView " + s + " = sequence.view();")
                        .collect(Collectors.joining("\n", "", ""));
        options = StringUtils.chomp(options.trim());
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(not(CsvToFieldInfoModel::isIndexField))
                        .map(s -> "    private int " + s.getFieldIdentifier() + " = " + s.getFieldIndex() + ";")
                        .collect(Collectors.joining("\n", "\n", ""));
        options = StringUtils.chomp(options.trim());
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isIndexField)
                        .map(s -> "    private final int " + s.getFieldIdentifier() + " = " + s.getFieldIndex() + ";")
                        .collect(Collectors.joining("\n", "\n", ""));
        options = StringUtils.chomp(options.trim());
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isConverterApplied)
                        .map(s -> "    private final " + s.getConverterClassName() + " \n            "
                                + s.getConverterInstanceId() + " = new " + s.getConverterClassName() + "();")
                        .collect(Collectors.joining("\n", "\n", ""));
        options = StringUtils.chomp(options.trim());
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isLookupApplied)
                        .map(s -> "    private Function<CharSequence, CharSequence> " + s.getLookupField() + " = Function.identity();")
                        .collect(Collectors.joining("\n", "\n", ""));
        options = StringUtils.chomp(options.trim());
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isValidated)
                        .map(CsvToFieldInfoModel::getValidatorDeclaration)
                        .collect(Collectors.joining("\n    ", "\n    ", ""));
        options = StringUtils.chomp(options.trim());
        return "    " + options;
    }

    private static String buildLookup(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.fieldInfoList().stream().anyMatch(CsvToFieldInfoModel::isLookupApplied)) {
            options = "    public " + codeGeneratorModel.getMarshallerClassName() + " addLookup(String lookupName, Function<CharSequence, CharSequence> lookup) {\n" +
                    "        switch(lookupName){\n";
            options +=
                    codeGeneratorModel.fieldInfoList().stream()
                            .filter(CsvToFieldInfoModel::isLookupApplied)
                            .map(s -> "            case  \"" + s.getLookupKey() + "\":\n"
                                    + "                " + s.getLookupField() + " = lookup;" +
                                    "\n                break;")
                            .collect(Collectors.joining("\n", "", "\n"));

            options += "             default:\n" +
                    "                if(!lookupName.equals(\"meta\"))\n" +
                    "                System.out.println(\"cannot find lookup with name:\" + lookupName);\n" +
                    "        }\n" +
                    "        return this;\n" +
                    "    }";
        }
        return options;
    }

    private static String buildCharacterProcessing(CodeGeneratorModel codeGeneratorModel) {
        String options = initMethod(codeGeneratorModel);
        options += charEventMethod(codeGeneratorModel);
        options += processEscapeSequenceMethod(codeGeneratorModel);
        options += processRowMethod(codeGeneratorModel);
        options += updateTargetMethod(codeGeneratorModel);
        options += buildLookup(codeGeneratorModel);
        options += mapHeaderMethod(codeGeneratorModel);
        options += writeHeadersMethod(codeGeneratorModel);
        options += writeInputHeadersMethod(codeGeneratorModel);
        options += writeRowMethod(codeGeneratorModel);
        return options;
    }

    @NotNull
    private static String initMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "@Override\n"
                + "    public void init() {\n"
                + "        super.init();\n";
        if (codeGeneratorModel.isNewBeanPerRecord()) {
            options += "        target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        } else {
            options += "        if (target==null){\n"
                    + "             target = new " + codeGeneratorModel.getTargetClassName() + "();\n"
                    + "        }\n";
        }
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(s -> "        fieldMap.put(" + s.getFieldIdentifier() + ", \"" + s.getTargetSetMethodName() + "\");")
                        .collect(Collectors.joining("\n", "", ""));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isConverterApplied)
                        .map(s -> s.getConverterInstanceId() + ".setConversionConfiguration(\"" + s.getConvertConfiguration() + "\");")
                        .collect(Collectors.joining("\n        ", "\n        ", ""));
        options = StringUtils.chomp(options.trim());
        options += "\n    }\n";
        return "    " + options;
    }

    @NotNull
    private static String charEventMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n    @Override\n"
                + "    public boolean charEvent(char character) {\n" +
                "        passedValidation = true;\n" +
                "        char charToTest = previousChar;\n" +
                "        previousChar = character;\n";
        if (codeGeneratorModel.isIgnoreQuotes()) {
            options += "    if (character == '\\\"'){\n" +
                    "        return false;\n" +
                    "    }\n";
        }
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "        if (!processChar(character)){\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        if (escaping) {\n" +
                    "            emptyRow = false;\n" +
                    "            return false;\n" +
                    "        }\n";
        }
        options += String.format(
                "        if (character == '\\r'){\n" +
                        "            return processRow();\n" +
                        "        }\n" +
                        "        if (character == '\\n' & charToTest != '\\r') {\n" +
                        "            return processRow();\n" +
                        "        }\n" +
                        "        if (character == '\\n'){\n" +
                        "            writtenLimit--;\n" +
                        "            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);\n" +
                        "            readPointer--;\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (character == '%c') {\n" +
                        "            updateFieldIndex();\n" +
                        "        }\n" +
                        "        emptyRow = false;\n" +
                        "        return false;\n" +
                        "    }\n", codeGeneratorModel.getDelimiter());
        return options;
    }

    private static String processEscapeSequenceMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "\n" +
                    "    private boolean processChar(char character) {\n" +
                    "        boolean charTest = firstCharOfField;\n" +
                    "        firstCharOfField = false;\n" +
                    "        boolean isQuote = character == '\"';\n" +
                    "        if (!charTest && !escaping) {\n" +
                    "            return true;\n" +
                    "        }\n" +
                    "        if (!escaping & isQuote) {//first quote\n" +
                    "            prevIsQuote = false;\n" +
                    "            escaping = true;\n" +
                    "            writtenLimit--;\n" +
                    "            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);\n" +
                    "            readPointer--;\n" +
                    "            return false;\n" +
                    "        } else if (escaping & !prevIsQuote & isQuote) {//possible termination\n" +
                    "            prevIsQuote = true;\n" +
                    "            return false;\n" +
                    "        } else if (escaping & prevIsQuote & !isQuote) {//actual termination\n" +
                    "            writtenLimit--;\n" +
                    "            System.arraycopy(chars, readPointer, chars, readPointer - 1, chars.length - readPointer);\n" +
                    "            readPointer--;\n" +
                    "            prevIsQuote = false;\n" +
                    "            escaping = false;\n" +
                    "        } else if (escaping & prevIsQuote & isQuote) {//an escaped quote\n" +
                    "            writtenLimit--;\n" +
                    "            System.arraycopy(chars, readPointer + 1, chars, readPointer, chars.length - readPointer - 1);\n" +
                    "            readPointer--;\n" +
                    "            prevIsQuote = false;\n" +
                    "        } \n" +
                    "        return true;\n" +
                    "    }\n" +
                    "    \n" +
                    "    protected boolean isEscaping(){\n" +
                    "       return escaping;\n" +
                    "    }\n" +
                    "    \n" +
                    "    protected boolean isPreviousAQuote(){\n" +
                    "       return prevIsQuote;\n" +
                    "    }\n" +
                    "\n"
            ;
        }
        return options;
    }

    private static String processRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n    @Override\n" +
                "    protected boolean processRow() {\n" +
                "        boolean targetChanged = false;\n" +
                "        rowNumber++;\n";
        if (codeGeneratorModel.isSkipCommentLines()) {
            options += "        if (sequence.charAt(delimiterIndex[0]) == '#'){\n" +
                    "            delimiterIndex[fieldIndex] = readPointer + 1;\n" +
                    "            fieldIndex = 0;\n" +
                    "            return targetChanged;\n" +
                    "        }\n";
        }
        if (codeGeneratorModel.isSkipEmptyLines()) {
            options += "        if (emptyRow){\n" +
                    "            removeCharFromBuffer();//delimiterIndex[fieldIndex] = readPointer + 1;\n" +
                    "            return targetChanged;\n" +
                    "        }\n";
        } else {
            if (codeGeneratorModel.isHeaderPresent()) {
                options += "        if (HEADER_ROWS < rowNumber & emptyRow)";
            } else {
                options += "        if (emptyRow)";
            }
            options += "{\n" +
                    "            removeCharFromBuffer();\n" +
                    (codeGeneratorModel.isAcceptPartials() ? "" : "            logProblem(\"empty lines are not valid input\");\n") +
                    (codeGeneratorModel.isAcceptPartials() ? "            fieldIndex = -1;\n" : "            fieldIndex = 0;\n") +
                    (codeGeneratorModel.isAcceptPartials() ? "" : "            return targetChanged;\n") +
                    "        }\n";
        }
        if (codeGeneratorModel.isHeaderPresent()) {
            options += "        if (HEADER_ROWS < rowNumber) {\n" +
                    "            targetChanged = updateTarget();\n" +
                    "        }\n";
        } else {
            options += "    targetChanged = updateTarget();";
        }
        if (codeGeneratorModel.isMappingRowPresent()) {
            options += "        if (rowNumber == MAPPING_ROW) {\n" +
                    "            mapHeader();\n" +
                    "        }\n";
        }
        options += "        fieldIndex = 0;\n" +
                "        delimiterIndex[fieldIndex] = readPointer + 1;\n" +
                "        emptyRow = true;\n" +
                "        return targetChanged;\n" +
                "    }\n";

        return options;
    }

    private static String updateTargetMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n" +
                "    private boolean updateTarget() {\n" +
                "        publish = true;\n" +
                "        int length = 0;\n";
        if (codeGeneratorModel.isNewBeanPerRecord()) {
            options += "        target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        }
        if (codeGeneratorModel.isAcceptPartials()) {
            options += "        maxFieldIndex = fieldIndex;\n";
        }
        options += "        try{\n" +
                "            updateFieldIndex();\n";
        if (codeGeneratorModel.isLoopAssignment()) {
            options += "                for (Runnable assignmentOperation : assignmentOperations) {\n" +
                    "                   assignmentOperation.run();\n" +
                    "               }\n";
        } else {
            options += fieldAssignment(codeGeneratorModel, false);
        }
        if (codeGeneratorModel.isPostProcessMethodSet()){
            options += "\n        target." + codeGeneratorModel.getPostProcessMethod() + "();\n";
        }
        options += "\n        } catch (Exception e) {\n" +
                "            logException(\"problem pushing '\"\n" +
                "                    + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex + 1] - 1).toString() + \"'\"\n" +
                "                    + \" from row:'\" +rowNumber +\"'\", false, e);\n" +
                "            passedValidation = false;\n" +
                "            return false;\n" +
                "        } finally {\n" +
                "            fieldIndex = 0;\n" +
                "        }\n" +
                "        return publish;\n" +
                "    }\n";
        return options;
    }

    private static String fieldAssignment(CodeGeneratorModel codeGeneratorModel, boolean isLoopAssignment) {
        final boolean acceptPartials = codeGeneratorModel.isAcceptPartials();
        final boolean trim = codeGeneratorModel.isTrim();
        Stream<String> assignmentStringStream = codeGeneratorModel.fieldInfoList().stream()
                .map(s -> {
                            String fieldIdentifier = s.getFieldIdentifier();
                            String readField = s.getTargetSetMethodName() + ".subSequenceNoOffset(delimiterIndex["
                                    + s.getFieldIdentifier() + "], delimiterIndex[" + fieldIdentifier + " + 1] - 1)";
                            String readOptionalFiled = s.getTargetSetMethodName() + ".subSequenceNoOffset(0,0)";
                            final boolean fieldTrim = s.isTrim() != trim;
                            if (fieldTrim) {
                                readField += ".trim();\n";
                                readOptionalFiled += ".trim();\n";
                            } else {
                                readField += ";\n";
                                readOptionalFiled += ";\n";
                            }
                            String out;
                            if (acceptPartials) {
                                out = String.format("            if (maxFieldIndex >= %s){\n", fieldIdentifier);
                                out += String.format("               fieldIndex = %s;\n", fieldIdentifier);
                            } else {
                                out = String.format("            fieldIndex = %s;\n", fieldIdentifier);
                            }

                            if (s.isDefaultOptionalField()) {
                                out += String.format("            if (fieldIndex > -1){\n" +
                                        "                %s" +
                                        "            }else{\n" +
                                        "                %s" +
                                        "            }\n", readField, readOptionalFiled);
                                out +=  "            " + s.getUpdateTarget();
                            } else if (s.isMandatory()) {
                                out += "            " + readField;
                                out += "            " + s.getUpdateTarget();
                            } else {
                                out += String.format("            if (fieldIndex > -1){\n" +
                                        "                %s" +
                                        "                %s\n" +
                                        "            }", readField, s.getUpdateTarget());
                            }
                            if (s.isValidated()) {
                                out += "\n            publish &= " + s.getValidatorInvocation();
                            }
                            if (acceptPartials) {
                                out += "\n            }";
                            }
                            if (isLoopAssignment){
                                out = out.replace("            ", "                    ");
                            }
                            return out;
                        }
                );
        if (isLoopAssignment) {
            return assignmentStringStream.collect(Collectors.joining(
                    "\n                },\n" +
                            "                () -> {\n",
                    "new Runnable[]{\n" +
                            "                () -> {\n",
                    "\n                }};\n"
            ));
        }
        return assignmentStringStream.collect(Collectors.joining("            \n"));
    }

    public static String mapHeaderMethod(CodeGeneratorModel codeGeneratorModel) {
        if (!codeGeneratorModel.isHeaderPresent()) {
            return "";
        }
        String options = "\n    private void mapHeader() {\n" +
                "        firstCharOfField = true;\n" +
                "        String header = new String(chars).trim().substring(delimiterIndex[0], readPointer);\n" +
                "        header = headerTransformer.apply(header);\n";
        options += String.format("        header = header.replace(\"\\\"\", \"\");\n" +
                "        List<String> headers = new ArrayList<>();\n" +
                "        for (String colName : header.split(Pattern.quote(\"%c\"))) {\n" +
                "            headers.add(getIdentifier(colName));\n" +
                "        }\n", codeGeneratorModel.getDelimiter());
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .map(s -> {
                            String out = String.format("        %1$s = headers.indexOf(\"%2$s\");\n" +
                                    "        fieldMap.put(%1$s, \"%3$s\");\n", s.getFieldIdentifier(), s.getSourceFieldName(), s.getTargetSetMethodName());
                            if (s.isMandatory()) {
                                out += String.format("        if (%s < 0) {\n" +
                                        "            logHeaderProblem(\"problem mapping field:'%s' missing column header, index row:\", true, null);\n" +
                                        "        }\n", s.getFieldIdentifier(), s.getSourceFieldName());
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining("", "", "    }\n"));
        return options;
    }

    public static String writeHeadersMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n    public void writeHeaders(StringBuilder builder) {\n";//
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .filter(CsvToFieldInfoModel::isWriteFieldToOutput)
                .map(s -> "        builder.append(\"" + s.getOutFieldName() + "\");")
                .collect(Collectors.joining(
                        "\n        builder.append(',');\n",
                        "",
                        "\n        builder.append('\\n');\n    }\n"));
        return options;
    }

    public static String writeInputHeadersMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n    public void writeInputHeaders(StringBuilder builder) {\n";//
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .filter(f -> !f.isDerived())
                .map(s -> "        builder.append(\"" + s.getSourceFieldName() + "\");")
                .collect(Collectors.joining(
                        "\n        builder.append(',');\n",
                        "",
                        "\n        builder.append('\\n');\n    }\n"));
        return options;
    }

    public static String writeRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = String.format(
                "\n    public void writeRow(%s target, StringBuilder builder) {\n", codeGeneratorModel.getTargetClassName());

        options += codeGeneratorModel.outputFieldInfoList().stream()
                .filter(FieldToCsvInfo::isWriteFieldToOutput)
                .map(FieldToCsvInfo::getWriteStatement)
                .collect(Collectors.joining(
                        "\n        builder.append(',');\n        ",
                        "        ",
                        "\n        builder.append('\\n');\n    }"));
        return options;
    }

    @SneakyThrows
    public void writeMarshaller() {
        String sourceString = String.format(CODE_TEMPLATE_DECLARATIONS,
                codeGeneratorModel.getPackageName(),
                codeGeneratorModel.getImports(),
                codeGeneratorModel.getMarshallerClassName(),
                codeGeneratorModel.getTargetClassName(),
                buildDeclarations(codeGeneratorModel),
                buildCharacterProcessing(codeGeneratorModel),
                codeGeneratorModel.isFailOnFirstError(),
                buildLoopAssignments(codeGeneratorModel)
        );
        if (codeGeneratorModel.isFormatSource()) {
            sourceString = CodeFormatter.formatJavaString(sourceString);
        }
        writer.write(sourceString);
    }

    static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>)target.negate();
    }
}
