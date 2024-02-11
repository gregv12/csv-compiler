package com.fluxtion.extension.csvcompiler;

//import com.fluxtion.extension.csvcompiler.TypeAnalyser.CandidateConverter.MyValidationLogger;
import com.fluxtion.extension.csvcompiler.converters.Conversion;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeAnalyser {

    public static final int MAX_COLUMNS_TO_ANALYSE = 100;
    private LongAdder failureCount = new LongAdder();
    private Map<String, String> defaultValueMap = new HashMap<>();

    private static SortedSet<CandidateConverter> candidateConverterSet() {
        return new TreeSet<>(Set.of(
                CandidateConverter.builder().priority(100).type("int").primitiveConverter(Conversion::atoi).build(),
                CandidateConverter.builder().priority(101).type("long").primitiveConverter(Conversion::atol).build(),
                CandidateConverter.builder().priority(102).type("double").primitiveConverter(Conversion::atod).build(),
                CandidateConverter.builder().priority(103).type("boolean").primitiveConverter(Conversion::atobool).build(),
                //have char after single numbers
                CandidateConverter.builder().priority(104).type("char").primitiveConverter(Conversion::atoc).build(),
                //default
                CandidateConverter.builder().priority(Integer.MAX_VALUE).type("String").primitiveConverter(Function.identity()).build()
        ));
    }

    @SneakyThrows
    public void defaultValues(File in){
        defaultValues(new FileReader(in));
    }

    public void defaultValues(String in){
        defaultValues(new StringReader(in));
    }

    public void defaultValues(Reader in){
        RowMarshaller.load(DefaultValue.class)
                .stream(in)
                .forEach(d -> defaultValueMap.put(d.getColumnName(), d.getDefaultValue()));
    }

    public CsvProcessingConfig analyse(String in) {
        return analyse(new StringReader(in));
    }

    @SneakyThrows
    public CsvProcessingConfig analyse(File in) {
        return analyse(new FileReader(in));
    }

    @SneakyThrows
    public CsvProcessingConfig analyse(Reader input) {
        BufferedReader bufferedReader = new BufferedReader(input);
        String first500Lines = bufferedReader.lines().limit(500).collect(Collectors.joining("\n"));
        input = new StringReader(first500Lines);

        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Analysis");
        csvProcessingConfig.setAcceptPartials(true);
        csvProcessingConfig.setProcessEscapeSequences(true);
        csvProcessingConfig.setHeaderLines(0);
        csvProcessingConfig.setMappingRow(0);
        for (int i = 0; i < MAX_COLUMNS_TO_ANALYSE; i++) {
            ColumnMapping columnMapping = new ColumnMapping();
            columnMapping.setSourceColumnIndex(i);
            csvProcessingConfig.getColumns().put("column_" + i, columnMapping);
        }
        //generate
        CsvChecker csvChecker = new CsvChecker(csvProcessingConfig);
        RowMarshaller<FieldAccessor> rowMarshaller = csvChecker.load();

        Iterator<FieldAccessor> fieldAccessorIterator = rowMarshaller.iterator(input);

        if (fieldAccessorIterator.hasNext()) {
            List<ColumnMapping> columnMappings = new ArrayList<>();
            List<AnalysedColumn> columnAnalysis = new ArrayList<>();
            FieldAccessor fieldAccessor = fieldAccessorIterator.next();
            FieldAccessor headerAccessor = fieldAccessor;
            int validColumnCount = 0;
            for (int i = 0; i < MAX_COLUMNS_TO_ANALYSE; i++) {
                validColumnCount = i;
                if (headerAccessor.getField("column_" + i) == null) {
                    break;
                }
            }

            for (int i = 0; i < validColumnCount; i++) {
                String field = headerAccessor.getField("column_" + i).toString();
                if (!field.isBlank()) {
                    columnAnalysis.add(new AnalysedColumn(field, defaultValueMap.getOrDefault(field, "")));
                    ColumnMapping columnMapping = new ColumnMapping();
                    columnMapping.setName(field);
                    columnMapping.setType("String");
                    columnMappings.add(columnMapping);
                }
            }

            CsvProcessingConfig csvProcessingConfigResult = new CsvProcessingConfig();
//            csvProcessingConfigResult.setDumpYaml(true);
//            csvProcessingConfigResult.setDumpGeneratedJava(true);
            csvProcessingConfigResult.setName("Analysis");
            csvProcessingConfigResult.setAcceptPartials(true);
            csvProcessingConfigResult.setProcessEscapeSequences(true);
            if (fieldAccessorIterator.hasNext()) {
                columnMappings.clear();
                while (fieldAccessorIterator.hasNext()) {
                    fieldAccessor = fieldAccessorIterator.next();
                    for (int i = 0; i < validColumnCount; i++) {
                        //TODO handle null field - need to set optional
                        String field = fieldAccessor.getField("column_" + i).toString();
                        columnAnalysis.get(i).pruneConverters(field);
                    }
                }
                columnAnalysis.stream().map(AnalysedColumn::asColumnMapping).forEach(columnMappings::add);
            }
            columnMappings.forEach(csvProcessingConfigResult::addColumnMapping);
            tryProcessFile(csvProcessingConfigResult, first500Lines);
            Yaml yaml = new Yaml();
            System.out.println("csv config:\n" + yaml.dump(csvProcessingConfigResult));
            return csvProcessingConfigResult;
        } else {
            throw new RuntimeException("must have at least one row to analyse");
        }
    }

    private void tryProcessFile(CsvProcessingConfig csvProcessingConfig, String testString) {
        boolean previousPartials = false;
        boolean previousEscape = false;
        //can we parse the file?
        MyValidationLogger validationLogger = new MyValidationLogger();
        csvProcessingConfig.setAcceptPartials(false);
        csvProcessingConfig.setProcessEscapeSequences(false);
        new CsvChecker(csvProcessingConfig).load()
                .setValidationLogger(validationLogger)
                .stream(new StringReader(testString))
                .count();
        if(failureCount.intValue() == 0){
            return;
        }

        csvProcessingConfig.setAcceptPartials(true);
        csvProcessingConfig.setProcessEscapeSequences(false);
        new CsvChecker(csvProcessingConfig).load()
                .setValidationLogger(validationLogger)
                .stream(new StringReader(testString))
                .count();

        if(failureCount.intValue() == 0){
            return;
        }

        csvProcessingConfig.setAcceptPartials(false);
        csvProcessingConfig.setProcessEscapeSequences(true);
        new CsvChecker(csvProcessingConfig).load()
                .setValidationLogger(validationLogger)
                .stream(new StringReader(testString))
                .count();

        if(failureCount.intValue() == 0){
            return;
        }

        csvProcessingConfig.setAcceptPartials(true);
        csvProcessingConfig.setProcessEscapeSequences(true);
        new CsvChecker(csvProcessingConfig).load()
                .setValidationLogger(validationLogger)
                .stream(new StringReader(testString))
                .count();

        if(failureCount.intValue() == 0){
            return;
        }
    }

    public static class AnalysedColumn {
        private final String name;
        private final SortedSet<CandidateConverter> candidateConverterSet;
        private final String defaultValue;

        public AnalysedColumn(String name) {
            this(name, "");
        }

        public AnalysedColumn(String name, String defaultValue) {
            this.name = name;
            this.candidateConverterSet = candidateConverterSet();
            this.defaultValue = defaultValue;
        }

        public void pruneConverters(CharSequence input) {
            candidateConverterSet.removeAll(
                    candidateConverterSet.stream()
                            .filter(c -> !c.testConversion(input))
                            .collect(Collectors.toSet())
            );
        }

        public CandidateConverter bestConverter() {
            return candidateConverterSet.first();
        }

        public ColumnMapping asColumnMapping() {
            ColumnMapping columnMapping = new ColumnMapping();
            CandidateConverter converter = bestConverter();
            columnMapping.setName(name);
            columnMapping.setType(converter.getType());
            columnMapping.setConverter(converter.getConverterClassName());
            columnMapping.setConverterConfiguration(converter.getConverterConfig());
            columnMapping.setTrimOverride(converter.isTrim());
            columnMapping.setOptional(converter.isOptional());
            columnMapping.setDefaultValue(defaultValue);
            return columnMapping;
        }
    }

    @Data(staticConstructor = "of")
    @Builder
    public static class CandidateConverter implements Comparable<CandidateConverter> {
        final String type;
        final FieldConverter<?> fieldConverter;
        String converterConfig = "";
        String defaultValue;
        int priority;
        boolean trim;
        boolean optional;
        private Function<CharSequence, ?> primitiveConverter;

        public String getName() {
            return fieldConverter.getId();
        }

        public boolean testConversion(CharSequence input, String defaultValue){
            return testConversion(input.isEmpty() ? defaultValue : input);
        }

        public boolean testConversion(CharSequence input) {

            if(input.length() == 0){
                optional = true;
                return true;
            }
            if (trim) {
                return testConversionWithTrim(input, true);
            } else if (!testConversionWithTrim(input, false)) {
                trim = true;
                return testConversionWithTrim(input, true);
            }
            return true;
        }

        private boolean testConversionWithTrim(CharSequence input, boolean trim) {
            if (trim) {
                input = input.toString().trim();
            }
            boolean successfulConversion = false;
            try {
                if (primitiveConverter != null) {
                    primitiveConverter.apply(input);
                } else if (fieldConverter != null) {
                    if (!StringUtils.isBlank(converterConfig)) {
                        fieldConverter.setConversionConfiguration(converterConfig);
                    }
                    fieldConverter.fromCharSequence(input);
                }
                successfulConversion = true;
            } catch (Throwable t) {
                //this is just catch the failed conversion and move on
            }
            return successfulConversion;
        }

        @Override
        public int compareTo(CandidateConverter otherCandidateConverter) {
            return priority - otherCandidateConverter.priority;
        }

        public String getConverterClassName() {
            return fieldConverter == null ? "" : fieldConverter.getClass().getCanonicalName();
        }

        public String getConverterConfig() {
            return converterConfig == null ? "" : converterConfig;
        }
    }

    private class MyValidationLogger implements ValidationLogger {
        @Override
        public void logFatal(CsvProcessingException csvProcessingException) {
            failureCount.increment();
        }

        @Override
        public void logWarning(CsvProcessingException csvProcessingException) {
            failureCount.increment();
        }
    }

}
