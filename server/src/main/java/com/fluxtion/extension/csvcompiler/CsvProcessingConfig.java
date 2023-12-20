package com.fluxtion.extension.csvcompiler;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private Map<String, ColumnMapping> columns = new LinkedHashMap<>();
    private Map<String, ColumnMapping> derivedColumns = new LinkedHashMap<>();
    private Map<String, ConversionFunction> conversionFunctions = new LinkedHashMap<>();
    private Map<String, ValidationFunction> validationFunctions = new LinkedHashMap<>();
    private Map<String, Map<String, String>>  lookupTables = new LinkedHashMap<>();
    private boolean dumpYaml = false;
    private boolean dumpGeneratedJava = false;

    public void addColumnMapping(ColumnMapping columnMapping){
        columns.put(columnMapping.getName(), columnMapping);
    }
}
