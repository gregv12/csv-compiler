package com.fluxtion.extension.csvcompiler;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
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
    private Map<String, ValidationFunction> validationFunctions = new HashMap<>();
    private Map<String, Map<String, String>> lookupTables = new HashMap<>();
}
