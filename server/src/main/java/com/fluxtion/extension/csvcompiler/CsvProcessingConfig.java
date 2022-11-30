package com.fluxtion.extension.csvcompiler;

import java.util.HashMap;
import java.util.Map;

public class CsvProcessingConfig {
    private String name;
    private int headerLines = 0;
    private int mappingRow = 1;
    boolean skipCommentLines = true;
    char fieldSeparator = ',';
    boolean acceptPartials = false;
    boolean skipEmptyLines = false;
    boolean ignoreQuotes = false;
    boolean failOnFirstError = false;
    boolean trim = false;
    boolean processEscapeSequences = false;
    private Map<String, ColumnMapping> columns = new HashMap<>();
    private Map<String, ColumnMapping> derivedColumns = new HashMap<>();
    private Map<String, ConversionFunction> conversionFunctions = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeaderLines() {
        return headerLines;
    }

    public void setHeaderLines(int headerLines) {
        this.headerLines = headerLines;
    }

    public int getMappingRow() {
        return mappingRow;
    }

    public void setMappingRow(int mappingRow) {
        this.mappingRow = mappingRow;
    }

    public boolean isSkipCommentLines() {
        return skipCommentLines;
    }

    public void setSkipCommentLines(boolean skipCommentLines) {
        this.skipCommentLines = skipCommentLines;
    }

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public boolean isAcceptPartials() {
        return acceptPartials;
    }

    public void setAcceptPartials(boolean acceptPartials) {
        this.acceptPartials = acceptPartials;
    }

    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    public void setSkipEmptyLines(boolean skipEmptyLines) {
        this.skipEmptyLines = skipEmptyLines;
    }

    public boolean isIgnoreQuotes() {
        return ignoreQuotes;
    }

    public void setIgnoreQuotes(boolean ignoreQuotes) {
        this.ignoreQuotes = ignoreQuotes;
    }

    public boolean isFailOnFirstError() {
        return failOnFirstError;
    }

    public void setFailOnFirstError(boolean failOnFirstError) {
        this.failOnFirstError = failOnFirstError;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public boolean isProcessEscapeSequences() {
        return processEscapeSequences;
    }

    public void setProcessEscapeSequences(boolean processEscapeSequences) {
        this.processEscapeSequences = processEscapeSequences;
    }

    public Map<String, ColumnMapping> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, ColumnMapping> columns) {
        this.columns = columns;
    }

    public Map<String, ConversionFunction> getConversionFunctions() {
        return conversionFunctions;
    }

    public void setConversionFunctions(Map<String, ConversionFunction> conversionFunctions) {
        this.conversionFunctions = conversionFunctions;
    }

    public Map<String, ColumnMapping> getDerivedColumns() {
        return derivedColumns;
    }

    public void setDerivedColumns(Map<String, ColumnMapping> derivedColumns) {
        this.derivedColumns = derivedColumns;
    }
}
