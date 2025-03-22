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
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class CodeGenerator {

    private static final String CODE_TEMPLATE_DECLARATIONS = "package %1$s;\n" +
            "%2$s\n" +
            "import static %1$s.%4$s.*;\n" +
            "\n" +
            "@AutoService(RowMarshaller.class)\n" +
            "public final class %3$s extends BaseMarshaller<%4$s>{\n" +
            "\n" +
            "%5$s\n" +
            "\n" +
            "    public %3$s() {\n" +
            "        super(%7$s);\n" +
            "    }\n" +
            "\n" +
            "@Override\n" +
            "    public Class<%4$s> targetClass(){\n" +
            "        return %4$s.class;\n" +
            "    }\n" +
            "\n" +
            "%6$s\n" +
            "\n" +
            "}\n";
    private final Writer writer;
    private final CodeGeneratorModel codeGeneratorModel;

    public CodeGenerator(Writer writer, CodeGeneratorModel codeGeneratorModel) {
        this.writer = writer;
        this.codeGeneratorModel = codeGeneratorModel;
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
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(CsvToFieldInfoModel::getTargetSetMethodName)
                        .map(s -> "    private final CharSequenceView " + s + " = sequence.view();")
                        .collect(Collectors.joining("\n", "\n", ""));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(not(CsvToFieldInfoModel::isIndexField))
                        .map(s -> "private int " + s.getFieldIdentifier() + " = " + s.getFieldIndex() + ";")
                        .collect(Collectors.joining("\n", "\n", ""));

        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isIndexField)
                        .map(s -> "private final int " + s.getFieldIdentifier() + " = " + s.getFieldIndex() + ";")
                        .collect(Collectors.joining("\n", "\n", ""));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isConverterApplied)
                        .map(s -> "private final " + s.getConverterClassName() + " " + s.getConverterInstanceId() + " = new " + s.getConverterClassName() + "();")
                        .collect(Collectors.joining("\n", "\n", ""));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isLookupApplied)
                        .map(s -> "private Function<CharSequence, CharSequence> " + s.getLookupField() + " = Function.identity();")
                        .collect(Collectors.joining("\n", "\n", ""));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isValidated)
                        .map(CsvToFieldInfoModel::getValidatorDeclaration)
                        .collect(Collectors.joining("\n", "\n", "\n"));
        return options;
    }

    private static String buildLookup(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.fieldInfoList().stream().anyMatch(CsvToFieldInfoModel::isLookupApplied)) {
            options = "public " + codeGeneratorModel.getMarshallerClassName() + " addLookup(String lookupName, Function<CharSequence, CharSequence> lookup){\n" +
                    "        switch(lookupName){\n";
            options +=
                    codeGeneratorModel.fieldInfoList().stream()
                            .filter(CsvToFieldInfoModel::isLookupApplied)
                            .map(s -> "    case  \"" + s.getLookupKey() + "\":\n\t" + s.getLookupField() + " = lookup;\nbreak;")
                            .collect(Collectors.joining("\n", "", "\n"));

            options +=
                    "         default:\n" +
                    "                if(!lookupName.equals(\"meta\"))\n" +
                    "                System.out.println(\"cannot find lookup with name:\" + lookupName);\n" +
                    "        }\n" +
                    "        return this;" +
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
                + "public void init(){\n"
                + "     super.init();\n";
        if (codeGeneratorModel.isNewBeanPerRecord()) {
            options += "target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        } else {
            options += "if(target==null){\n"
                    + "     target = new " + codeGeneratorModel.getTargetClassName() + "();\n"
                    + "}\n";
        }
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(s -> "fieldMap.put(" + s.getFieldIdentifier() + ", \"" + s.getTargetSetMethodName() + "\");")
                        .collect(Collectors.joining("\n", "", "\n"));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .filter(CsvToFieldInfoModel::isConverterApplied)
                        .map(s -> s.getConverterInstanceId() + ".setConversionConfiguration(\"" + s.getConvertConfiguration() + "\");")
                        .collect(Collectors.joining("\n", "", "\n}"));
        return options;
    }

    @NotNull
    private static String charEventMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "@Override\n"
                + "    public boolean charEvent(char character) {\n" +
                "        passedValidation = true;\n" +
                "        char charToTest = previousChar;\n" +
                "        previousChar = character;\n";
        if (codeGeneratorModel.isIgnoreQuotes()) {
            options += "    if(character == '\\\"'){\n" +
                    "        return false;\n" +
                    "    }\n";
        }
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "    if(!processChar(character)){\n" +
                    "        return false;\n" +
                    "    }\n" +
                    "    if (escaping) {\n" +
                    "        chars[writeIndex++] = character;\n" +
                    "        return false;\n" +
                    "    }\n";
        }
        options += String.format(
                "        if(character == '\\r'){\n" +
                        "            return processRow();\n" +
                        "        }\n" +
                        "        if (character == '\\n' & charToTest != '\\r') {\n" +
                        "            return processRow();\n" +
                        "        }\n" +
                        "        if(character == '\\n'){\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (character == '%c') {\n" +
                        "            updateFieldIndex();\n" +
                        "        }\n" +
                        "        chars[writeIndex++] = character;\n" +
                        "        return false;\n" +
                        "    }\n", codeGeneratorModel.getDelimiter());
        return options;
    }

    private static String processEscapeSequenceMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "" +
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
                    "            return false;\n" +
                    "        } else if (escaping & !prevIsQuote & isQuote) {//possible termination\n" +
                    "            prevIsQuote = true;\n" +
                    "            return false;\n" +
                    "        } else if (escaping & prevIsQuote & !isQuote) {//actual termination\n" +
                    "            prevIsQuote = false;\n" +
                    "            escaping = false;\n" +
                    "        } else if (escaping & prevIsQuote & isQuote) {//an escaped quote\n" +
                    "            prevIsQuote = false;\n" +
                    "        } \n" +
                    "        return true;\n" +
                    "    }\n";
        }
        return options;
    }

    private static String processRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "@Override\n" +
                "    protected boolean processRow() {\n" +
                "        boolean targetChanged = false;\n" +
                "        rowNumber++;\n";
        if (codeGeneratorModel.isSkipCommentLines()) {
            options += "if(chars[0]=='#'){\n" +
                    "    writeIndex = 0;\n" +
                    "    fieldIndex = 0;\n" +
                    "    return targetChanged;\n" +
                    "}\n";
        }
        if (codeGeneratorModel.isSkipEmptyLines()) {
            options += "if(writeIndex < 1){\n" +
                    "        writeIndex = 0;\n" +
                    "        fieldIndex = 0;\n" +
                    "        return targetChanged;\n" +
                    "    }\n";
        } else {
            options += "if(writeIndex < 1){\n" +
                    (codeGeneratorModel.isAcceptPartials() ? "" : "        logProblem(\"empty lines are not valid input\");\n") +
                    "        writeIndex = 0;\n" +
                    "        fieldIndex = 0;\n" +
                    (codeGeneratorModel.isAcceptPartials() ? "" : "        return targetChanged;\n") +
                    "    }\n";
        }
        if (codeGeneratorModel.isHeaderPresent()) {
            options += "    if (HEADER_ROWS < rowNumber) {\n" +
                    "        targetChanged = updateTarget();\n" +
                    "    }\n";
        } else {
            options += "    targetChanged = updateTarget();";
        }
        if (codeGeneratorModel.isMappingRowPresent()) {
            options += "    if (rowNumber==MAPPING_ROW) {\n" +
                    "        mapHeader();\n" +
                    "    }\n";
        }
        options += "    writeIndex = 0;\n" +
                "    fieldIndex = 0;\n" +
                "    return targetChanged;\n" +
                "}\n";

        return options;
    }

    private static String updateTargetMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n" +
                "private boolean updateTarget() {\n" +
                "    boolean publish = true;\n" +
                "    int length = 0;\n";
        if (codeGeneratorModel.isNewBeanPerRecord()) {
            options += "target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        }
        if (codeGeneratorModel.isAcceptPartials()) {
            options += "int maxFieldIndex = fieldIndex;\n";
        }
        options += "try{\n" +
                "    updateFieldIndex();\n";
        final boolean acceptPartials = codeGeneratorModel.isAcceptPartials();
        final boolean trim = codeGeneratorModel.isTrim();
        options += codeGeneratorModel.fieldInfoList().stream()
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
                                out = String.format("if (maxFieldIndex >= %s ){", fieldIdentifier);
                            } else {
                                out = String.format("fieldIndex = %s;", fieldIdentifier);
                            }

                            if (s.isDefaultOptionalField()) {
                                out += String.format("if(fieldIndex > -1){\n" +
                                        "    %s\n" +
                                        "}else{\n" +
                                        "    %s\n" +
                                        "}\n", readField, readOptionalFiled);
                                out += s.getUpdateTarget();
                            } else if (s.isMandatory()) {
                                out += readField;
                                out += s.getUpdateTarget();
                            } else {
                                out += String.format("if(fieldIndex > -1){\n" +
                                        "    %s\n" +
                                        "    %s\n" +
                                        "}\n", readField, s.getUpdateTarget());
                            }
                            if (s.isValidated()) {
                                out += "publish = " + s.getValidatorInvocation();
                            }
                            if (acceptPartials) {
                                out += "}";
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining(""));

        if (codeGeneratorModel.isPostProcessMethodSet())
            options += "\ntarget." + codeGeneratorModel.getPostProcessMethod() + "();\n";

        options += "    } catch (Exception e) {\n" +
                "        logException(\"problem pushing '\"\n" +
                "                + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex + 1] - 1).toString() + \"'\"\n" +
                "                + \" from row:'\" +rowNumber +\"'\", false, e);\n" +
                "        passedValidation = false;\n" +
                "        return false;\n" +
                "    } finally {\n" +
                "        fieldIndex = 0;\n" +
                "    }\n" +
                "    return publish;\n" +
                "}\n";
        return options;
    }

    public static String mapHeaderMethod(CodeGeneratorModel codeGeneratorModel) {
        if (!codeGeneratorModel.isHeaderPresent()) {
            return "";
        }
        String options = "    private void mapHeader(){\n" +
                "        firstCharOfField = true;\n" +
                "        String header = new String(chars).trim().substring(0, writeIndex);\n" +
                "        header = headerTransformer.apply(header);\n";
        options += String.format("        header = header.replace(\"\\\"\", \"\");\n" +
                "        List<String> headers = new ArrayList<>();\n" +
                "        for (String colName : header.split(Pattern.quote(\"%c\"))) {\n" +
                "            headers.add(getIdentifier(colName));\n" +
                "        }\n", codeGeneratorModel.getDelimiter());
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .map(s -> {
                            String out = String.format("%1$s = headers.indexOf(\"%2$s\");\n" +
                                    "fieldMap.put(%1$s, \"%3$s\");\n", s.getFieldIdentifier(), s.getSourceFieldName(), s.getTargetSetMethodName());
                            if (s.isMandatory()) {
                                out += String.format("    if (%s < 0) {\n" +
                                        "        logHeaderProblem(\"problem mapping field:'%s' missing column header, index row:\", true, null);\n" +
                                        "    }\n", s.getFieldIdentifier(), s.getSourceFieldName());
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining("", "", "}"));
        return options;
    }

    public static String writeHeadersMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "    public void writeHeaders( StringBuilder builder){\n";//
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .filter(CsvToFieldInfoModel::isWriteFieldToOutput)
                .map(s -> "builder.append(\"" + s.getOutFieldName() + s.isWriteFieldToOutput() +"\");")
                .collect(Collectors.joining(
                        "\nbuilder.append(',');\n",
                        "",
                        "builder.append('\\n');\n}"));
        return options;
    }

    public static String writeInputHeadersMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "    public void writeInputHeaders( StringBuilder builder){\n";//
        options += codeGeneratorModel.fieldInfoList().stream()
                .filter(f -> !f.isIndexField())
                .filter(f -> !f.isDerived())
                .map(s -> "builder.append(\"" + s.getSourceFieldName() + "\");")
                .collect(Collectors.joining(
                        "\nbuilder.append(',');\n",
                        "",
                        "builder.append('\\n');\n}"));
        return options;
    }

    public static String writeRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = String.format(
                "    public void writeRow(%s target, StringBuilder builder){\n", codeGeneratorModel.getTargetClassName());

        options += codeGeneratorModel.outputFieldInfoList().stream()
                .filter(FieldToCsvInfo::isWriteFieldToOutput)
                .map(FieldToCsvInfo::getWriteStatement)
//                .map(s -> "builder.append(target." + s.getTargetGetMethodName() + "());")
                .collect(Collectors.joining(
                        "\nbuilder.append(',');\n",
                        "",
                        "builder.append('\\n');\n}"));
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
                codeGeneratorModel.isFailOnFirstError()
        );
        sourceString = sourceString.replace("\n\n\n\n\n\n\n", "\n");
        sourceString = sourceString.replace("\n\n\n\n\n\n", "\n");
        sourceString = sourceString.replace("\n\n\n\n\n", "\n");
        sourceString = sourceString.replace("\n\n\n\n", "\n");
        sourceString = sourceString.replace("\n\n\n", "\n");
        sourceString = sourceString.replace("\n\n", "\n");
        sourceString = sourceString.replace("\n", "\n   ");
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
