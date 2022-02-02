package com.fluxtion.extension.csvcompiler.processor.model;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import lombok.SneakyThrows;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.stream.Collectors;


public class CodeGenerator {

    private static final String CODE_TEMPLATE_DECLARATIONS = "package %s;\n" +
                                                             "\n" +
                                                             "%s\n" +
                                                             "\n" +
                                                             "@AutoService(CsvMarshallerLoader.class)\n" +
                                                             "public class %s implements CsvMarshallerLoader<%4$s>{\n" +
                                                             "\n" +
                                                             "    private %s target;\n" +
                                                             "    private int rowNumber;\n" +
                                                             "    private final HashMap fieldMap = new HashMap<>();\n" +
                                                             "    private boolean passedValidation;\n" +
                                                             "    private ValidationLogger errorLog = ValidationLogger.CONSOLE;\n" +
                                                             "    private final char[] chars = new char[4096];\n" +
                                                             "    private final int[] delimiterIndex = new int[1024];\n" +
                                                             "    private StringBuilder messageSink = new StringBuilder(256);\n" +
                                                             "    private final CharArrayCharSequence sequence = new CharArrayCharSequence(chars);\n" +
                                                             "    private int fieldIndex = 0;\n" +
                                                             "    private int writeIndex = 0;\n" +
                                                             "    private boolean failOnError = %7$s;\n" +
                                                             "\n" +
                                                             "%s\n" +
                                                             "\n" +
                                                             "    public Class<%4$s> targetClass(){\n" +
                                                             "        return %4$s.class;\n" +
                                                             "    }\n" +
                                                             "\n" +
                                                             "%s\n" +
                                                             "\n" +
                                                             "    public boolean passedValidation() {\n" +
                                                             "        return passedValidation;\n" +
                                                             "    }\n" +
                                                             "\n" +
                                                             "    public int getRowNumber() {\n" +
                                                             "        return rowNumber;\n" +
                                                             "    }\n" +
                                                             "\n" +
                                                             "    @Override\n" +
                                                             "    public CsvMarshallerLoader<%4$s> setErrorLog(ValidationLogger errorLog) {\n" +
                                                             "        this.errorLog = errorLog;\n" +
                                                             "        return this;\n" +
                                                             "    }\n" +
                                                             "\n" +
                                                             "    @Override\n" +
                                                             "    public void stream(Consumer<%4$s> consumer, Reader in) {\n" +
                                                             "        init();\n" +
                                                             "        int c;\n" +
                                                             "        try {\n" +
                                                             "            while ((c = in.read()) != -1) {\n" +
                                                             "                if (charEvent((char) c)) {\n" +
                                                             "                    consumer.accept(target);\n" +
                                                             "                }\n" +
                                                             "            }\n" +
                                                             "            eof();\n" +
                                                             "        }catch (IOException e){\n" +
                                                             "            throw new RuntimeException(e);\n" +
                                                             "        }\n" +
                                                             "    }\n" +
                                                             "}\n";
    private final Writer writer;
    private final CodeGeneratorModel codeGeneratorModel;

    public CodeGenerator(Writer writer, CodeGeneratorModel codeGeneratorModel) {
        this.writer = writer;
        this.codeGeneratorModel = codeGeneratorModel;
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
        if (codeGeneratorModel.isFormatSource()) {
            sourceString = new Formatter(
                    JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build()
            ).formatSource(sourceString);
        }
        writer.write(sourceString);
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
                        .map(CsvToFieldInfoModel::getTargetCalcMethodName)
                        .map(s -> "    private final CharSequenceView " + s + " = sequence.view();")
                        .collect(Collectors.joining("\n"));
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(s -> "private int " + s.getFieldIdentifier() + " = " + s.getFieldIndex() + ";")
                        .collect(Collectors.joining("\n"));
        return options;
    }

    private static String buildCharacterProcessing(CodeGeneratorModel codeGeneratorModel) {
        String options = initMethod(codeGeneratorModel);
        options += charEventMethod(codeGeneratorModel);
        options += eofMethod();
        options += processEscapeSequenceMethod(codeGeneratorModel);
        options += processRowMethod(codeGeneratorModel);
        options += updateTargetMethod(codeGeneratorModel);
        options += mapHeaderMethod(codeGeneratorModel);
        options += updateFieldIndexMethod();
        options += logErrorMethods(codeGeneratorModel);
        return options;
    }

    @NotNull
    private static String initMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "public void init(){\n" +
                         "target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        options +=
                codeGeneratorModel.fieldInfoList().stream()
                        .map(s -> "fieldMap.put(" + s.getFieldIdentifier() + ", \"" + s.getTargetCalcMethodName() + "\");")
                        .collect(Collectors.joining("\n", "", "}"));
        return options;
    }

    @NotNull
    private static String charEventMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = String.format("    public boolean charEvent(char character) {\n" +
                                       "        passedValidation = true;\n" +
                                       "        if(character == '%s'){\n" +
                                       "            return false;\n" +
                                       "        }\n", StringEscapeUtils.escapeJava(codeGeneratorModel.getIgnoreCharacter() + ""));
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
        options += String.format("        if (character == '%s') {\n" +
                                 "            return processRow();\n" +
                                 "        }\n" +
                                 "        if (character == '%c') {\n" +
                                 "            updateFieldIndex();\n" +
                                 "        }\n" +
                                 "        chars[writeIndex++] = character;\n" +
                                 "        return false;\n" +
                                 "    }\n", StringEscapeUtils.escapeJava(codeGeneratorModel.getNewLineCharacter() + "" ), codeGeneratorModel.getDelimiter());
        return options;
    }

    private static String processEscapeSequenceMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += "    private boolean processChar(char character){\n" +
                       "        boolean isQuote = character == '\"';\n" +
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

    private static String eofMethod() {
        return "\n" +
               "    public boolean eof(){\n" +
               "        return writeIndex==0?false:processRow();\n" +
               "    }\n";
    }

    private static String processRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "\n" +
                         "    private boolean processRow() {\n" +
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
        }else{
            options += "if(writeIndex < 1){\n" +
                    "        logProblem(\"empty lines are not valid input\");\n" +
                    "        writeIndex = 0;\n" +
                    "        fieldIndex = 0;\n" +
                    "        return targetChanged;\n" +
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
                            String readField = s.getTargetCalcMethodName() + ".subSequenceNoOffset(delimiterIndex["
                                               + s.getFieldIdentifier() + "], delimiterIndex[" + fieldIdentifier + " + 1] - 1)";
                            String readOptionalFiled = s.getTargetCalcMethodName() + ".subSequenceNoOffset(0,0)";
                            if (trim) {
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
                            } else if (s.isMandatory()) {
                                out += readField;
                            } else {
                                out += String.format("if(fieldIndex > -1){\n" +
                                                     "    %s\n" +
                                                     "}\n", readField);
                            }
                            out += s.getUpdateTarget();
                            if (acceptPartials) {
                                out += "}";
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining(""));

        options += "    } catch (Exception e) {\n" +
                   "        logException(\"problem pushing '\"\n" +
                   "                + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex + 1] - 1).toString() + \"'\"\n" +
                   "                + \" from row:'\" +rowNumber +\"'\", false, e);\n" +
                   "        passedValidation = false;\n" +
                   "        return false;\n" +
                   "    } finally {\n" +
                   "        fieldIndex = 0;\n" +
                   "    }\n" +
                   "    return true;\n" +
                   "}\n";
        return options;
    }

    public static String mapHeaderMethod(CodeGeneratorModel codeGeneratorModel) {
        if (!codeGeneratorModel.isHeaderPresent()) {
            return "";
        }
        String options = "    private void mapHeader(){\n" +
                         "        String header = new String(chars).trim().substring(0, writeIndex);\n";
        if (codeGeneratorModel.isAsciiOnlyHeader()) {
//            options += "    header = header.replaceAll(\"\\P{InBasic_Latin}\", \"\");";
        }
        options += String.format("        header = header.replace(\"\\\"\", \"\");\n" +
                                 "        List<String> headers = new ArrayList();\n" +
                                 "        for (String colName : header.split(Pattern.quote(\"%c\"))) {\n" +
                                 "            headers.add(getIdentifier(colName));\n" +
                                 "        }\n", codeGeneratorModel.getDelimiter());
        options += codeGeneratorModel.fieldInfoList().stream()
                .map(s -> {
                            String out = String.format("%1$s = headers.indexOf(\"%2$s\");\n" +
                                                       "fieldMap.put(%1$s, \"%3$s\");\n", s.getFieldIdentifier(), s.getFieldName(), s.getTargetCalcMethodName());
                            if (s.isMandatory()) {
                                out += String.format("    if (%s < 0) {\n" +
                                                     "        logHeaderProblem(\"problem mapping field:'%s' missing column header, index row:\", true, null);\n" +
                                                     "    }\n", s.getFieldIdentifier(), s.getFieldName());
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining("", "", "}"));
        return options;
    }

    public static String logErrorMethods(CodeGeneratorModel codeGeneratorModel) {
        return String.format("    private void logException(String prefix, boolean fatal, Exception e) {\n" +
                             "         StringBuilder sb = new StringBuilder()\n" +
                             "                .append(\"%1$s \")\n" +
                             "                .append(prefix)\n" +
                             "                .append(\" fieldIndex:'\")\n" +
                             "                .append(fieldIndex)\n" +
                             "                .append(\"' targetMethod:'%1$s#\")\n" +
                             "                .append(fieldMap.get(fieldIndex))\n" +
                             "                .append(\"' error:'\")\n" +
                             "                .append(e.toString())\n" +
                             "                .append(\"'\")\n" +
                             "                ;\n" +
                             "        CsvProcessingException csvProcessingException = new CsvProcessingException(sb.toString(), e, rowNumber);\n" +
                             "        if (fatal || failOnError) {\n" +
                             "            errorLog.logFatal(csvProcessingException);\n" +
                             "            throw csvProcessingException;\n" +
                             "        }\n" +
                             "        errorLog.logException(csvProcessingException);" +
                             "    }\n" +
                             "\n" +
                             " private void logProblem(String description){\n" +
                            "       CsvProcessingException csvProcessingException = new CsvProcessingException(description, rowNumber);\n" +
                            "        if (failOnError) {\n" +
                            "            errorLog.logFatal(csvProcessingException);\n" +
                            "            throw csvProcessingException;\n" +
                            "        }\n" +
                            "        errorLog.logException(csvProcessingException);\n" +
                            "    }\n" +
                             "    private void logHeaderProblem(String prefix, boolean fatal, Exception e) {\n" +
                             "        StringBuilder sb = new StringBuilder().append(\"%1$s \").append(prefix).append(rowNumber);\n" +
                             "        CsvProcessingException csvProcessingException =\n" +
                             "                new CsvProcessingException(sb.toString(), e, rowNumber);\n" +
                             "        if (fatal || failOnError) {\n" +
                             "            errorLog.logFatal(csvProcessingException);\n" +
                             "            throw csvProcessingException;\n" +
                             "        }\n" +
                             "        errorLog.logException(csvProcessingException);\n" +
                             "    }\n", codeGeneratorModel.getTargetClassName());
    }

    private static String updateFieldIndexMethod() {
        return "    private void updateFieldIndex() {\n" +
               "        fieldIndex++;\n" +
               "        delimiterIndex[fieldIndex] = writeIndex + 1;\n" +
               "    }\n";
    }


}
