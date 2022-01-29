package com.fluxtion.extension.csvcompiler.processor.model;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.stream.Collectors;


public class CodeGenerator {

    private static final String CODE_TEMPLATE_DECLARATIONS = """
            package %s;

            %s
            
            @AutoService(CsvMarshallerLoader.class)
            public class %s implements CsvMarshallerLoader<%4$s>{
                        
                private %s target;
                private int rowNumber;
                private final HashMap fieldMap = new HashMap<>();
                private boolean passedValidation;
                private ValidationLogger errorLog = ValidationLogger.CONSOLE;
                private final char[] chars = new char[4096];
                private final int[] delimiterIndex = new int[1024];
                private StringBuilder messageSink = new StringBuilder(256);
                private final CharArrayCharSequence sequence = new CharArrayCharSequence(chars);
                private int fieldIndex = 0;
                private int writeIndex = 0;
                
            %s
                
                public Class<%4$s> targetClass(){
                    return %4$s.class;
                }
                
            %s
                        
                public boolean passedValidation() {
                    return passedValidation;
                }
                        
                public int getRowNumber() {
                    return rowNumber;
                }
            
                @Override
                public CsvMarshallerLoader<%4$s> setErrorLog(ValidationLogger errorLog) {
                    this.errorLog = errorLog;
                    return this;
                }
                
                @Override
                public void stream(Consumer<%4$s> consumer, Reader in) {
                    init();
                    int c;
                    try {
                        while ((c = in.read()) != -1) {
                            if (charEvent((char) c)) {
                                consumer.accept(target);
                            }
                        }
                        eof();
                    }catch (IOException e){
                        throw new RuntimeException(e);
                    }
                }
            }
            """;
    private final Writer writer;
    private final CodeGeneratorModel codeGeneratorModel;

    public CodeGenerator(Writer writer, CodeGeneratorModel codeGeneratorModel) {
        this.writer = writer;
        this.codeGeneratorModel = codeGeneratorModel;
    }

    @SneakyThrows
    public void writeMarshaller() {
        String sourceString = CODE_TEMPLATE_DECLARATIONS.formatted(
                codeGeneratorModel.getPackageName(),
                codeGeneratorModel.getImports(),
                codeGeneratorModel.getMarshallerClassName(),
                codeGeneratorModel.getTargetClassName(),
                buildDeclarations(codeGeneratorModel),
                buildCharacterProcessing(codeGeneratorModel)
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
        String options = """
                    public boolean charEvent(char character) {
                        passedValidation = true;
                        if(character == '%s'){
                            return false;
                        }
                """.formatted(StringEscapeUtils.escapeJava(codeGeneratorModel.getIgnoreCharacter() + ""));
        if (codeGeneratorModel.isIgnoreQuotes()) {
            options += """
                        if(character == '\\"'){
                            return false;
                        }
                    """;
        }
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += """
                        if(!processChar(character)){
                            return false;
                        }
                        if (escaping) {
                            chars[writeIndex++] = character;
                            return false;
                        }
                    """;
        }
        options += """
                        if (character == '%s') {
                            return processRow();
                        }
                        if (character == '%c') {
                            updateFieldIndex();
                        }
                        chars[writeIndex++] = character;
                        return false;
                    }
                """.formatted(StringEscapeUtils.escapeJava("\n"), codeGeneratorModel.getDelimiter());
        return options;
    }

    private static String processEscapeSequenceMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = "";
        if (codeGeneratorModel.isProcessEscapeSequence()) {
            options += """
                        private boolean processChar(char character){
                            boolean isQuote = character == '"';
                            if (!escaping & isQuote) {//first quote
                                prevIsQuote = false;
                                escaping = true;
                                return false;
                            } else if (escaping & !prevIsQuote & isQuote) {//possible termination
                                prevIsQuote = true;
                                return false;
                            } else if (escaping & prevIsQuote & !isQuote) {//actual termination
                                prevIsQuote = false;
                                escaping = false;
                            } else if (escaping & prevIsQuote & isQuote) {//an escaped quote
                                prevIsQuote = false;
                            }\s
                            return true;
                        }
                    """;
        }
        return options;
    }

    private static String eofMethod() {
        return """
                    
                    public boolean eof(){
                        return writeIndex==0?false:processRow();
                    }
                """;
    }

    private static String processRowMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = """
                                
                    private boolean processRow() {
                        boolean targetChanged = false;
                        rowNumber++;
                """;
        if (codeGeneratorModel.isSkipCommentLines()) {
            options += """
                        if(chars[0]=='#'){
                            writeIndex = 0;
                            fieldIndex = 0;
                            return targetChanged;    
                        }
                    """;
        }
        if (codeGeneratorModel.isHeaderPresent() && codeGeneratorModel.isSkipCommentLines()) {
            options += """
                        if (HEADER_ROWS < rowNumber & writeIndex > 0) {
                            targetChanged = updateTarget();
                        }
                    """;
        } else if (codeGeneratorModel.isHeaderPresent()) {
            options += """
                        if (HEADER_ROWS < rowNumber) {
                            targetChanged = updateTarget();
                        }
                    """;
        } else if (!codeGeneratorModel.isHeaderPresent() && codeGeneratorModel.isSkipCommentLines()) {
            options += """
                        if(writeIndex > 0){
                            targetChanged = updateTarget();
                        }
                    """;
        } else {
            options += "    targetChanged = updateTarget();";
        }
        if (codeGeneratorModel.isMappingRowPresent()) {
            options += """
                        if (rowNumber==MAPPING_ROW) {
                            mapHeader();
                        }
                    """;
        }
        options += """
                    writeIndex = 0;
                    fieldIndex = 0;
                    return targetChanged;    
                }
                """;

        return options;
    }

    private static String updateTargetMethod(CodeGeneratorModel codeGeneratorModel) {
        String options = """
                                
                private boolean updateTarget() {
                    int length = 0;
                """;
        if (codeGeneratorModel.isNewBeanPerRecord()) {
            options += "target = new " + codeGeneratorModel.getTargetClassName() + "();\n";
        }
        if (codeGeneratorModel.isAcceptPartials()) {
            options += "int maxFieldIndex = fieldIndex;\n";
        }
        options += """
                try{
                    updateFieldIndex();
                """;
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
                                out = "if (maxFieldIndex > %s {" .formatted(fieldIdentifier);
                            } else {
                                out = "fieldIndex = %s;" .formatted(fieldIdentifier);
                            }

                            if (s.isDefaultOptionalField()) {
                                out += """
                                        if(fieldIndex > -1){
                                            %s
                                        }else{
                                            %s
                                        }
                                        """.formatted(readField, readOptionalFiled);
                            } else if (s.isMandatory()) {
                                out += readField;
                            } else {
                                out += """
                                        if(fieldIndex > -1){
                                            %s
                                        }
                                        """.formatted(readField);
                            }
                            out += s.getUpdateTarget();
                            if (acceptPartials) {
                                out += "}";
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining(""));

        options += """
                    } catch (Exception e) {
                        logException("problem pushing '"
                                + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex + 1] - 1).toString() + "'"
                                + " from row:'" +rowNumber +"'", false, e);
                        passedValidation = false;
                        return false;
                    } finally {
                        fieldIndex = 0;
                    }
                    return true;
                }
                """;
        return options;
    }

    public static String mapHeaderMethod(CodeGeneratorModel codeGeneratorModel) {
        if (!codeGeneratorModel.isHeaderPresent()) {
            return "";
        }
        String options = """
                    private void mapHeader(){
                        String header = new String(chars).trim();
                """;
        if (codeGeneratorModel.isAsciiOnlyHeader()) {
//            options += "    header = header.replaceAll(\"\\P{InBasic_Latin}\", \"\");";
        }
        options += """
                        header = header.replace("\\"", "");
                        List<String> headers = new ArrayList();
                        for (String colName : header.split("%c")) {
                            headers.add(getIdentifier(colName));
                        }
                """.formatted(codeGeneratorModel.getDelimiter());
        options += codeGeneratorModel.fieldInfoList().stream()
                .map(s -> {
                            String out = """
                                    %1$s = headers.indexOf("%2$s");
                                    fieldMap.put(%1$s, "%3$s");
                                    """.formatted(s.getFieldIdentifier(), s.getFieldName(), s.getTargetCalcMethodName());
                            if (s.isMandatory()) {
                                out += """
                                            if (%s < 0) {
                                                logHeaderProblem("problem mapping field:'%s' missing column header, index row:", true, null);
                                            }
                                        """.formatted(s.getFieldIdentifier(), s.getFieldName());
                            }
                            return out;
                        }
                )
                .collect(Collectors.joining("", "", "}"));
        return options;
    }

    public static String logErrorMethods(CodeGeneratorModel codeGeneratorModel) {
        return """
                    private void logException(String prefix, boolean fatal, Exception e) {
                         StringBuilder sb = new StringBuilder()
                                .append("%1$s ")
                                .append(prefix)
                                .append(" fieldIndex:'")
                                .append(fieldIndex)
                                .append("' targetMethod:'%1$s#")
                                .append(fieldMap.get(fieldIndex))
                                .append("' error:'")
                                .append(e.toString())
                                .append("'")
                                ;
                        if(fatal){
                            errorLog.logFatal(sb);
                            throw new RuntimeException(sb.toString(), e);
                        }
                        errorLog.logException(sb);
                    }
                                
                    private void logHeaderProblem(String prefix, boolean fatal, Exception e) {
                        StringBuilder sb = new StringBuilder().append("%1$s ").append(prefix).append(rowNumber);
                        if(fatal){
                            errorLog.logFatal(sb);
                            throw new RuntimeException(sb.toString(), e);
                        }
                        errorLog.logException(sb);
                    }
                """.formatted(codeGeneratorModel.getTargetClassName());
    }

    private static String updateFieldIndexMethod() {
        return """
                    private void updateFieldIndex() {
                        fieldIndex++;
                        delimiterIndex[fieldIndex] = writeIndex + 1;
                    }
                """;
    }


}
