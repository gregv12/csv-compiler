package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.CsvChecker;
import com.fluxtion.extension.csvcompiler.CsvProcessingConfig;
import com.fluxtion.extension.csvcompiler.TypeAnalyser;
import org.junit.jupiter.api.Test;

public class TypeAnalyserTest {

    @Test
    public void simpleFileAnalysis() {
        String data = """
                name,age,percentage
                greg,12,45.8
                tim,90,22
                """;

        TypeAnalyser analyser = new TypeAnalyser();
        analyser.analyse(data);
    }

    @Test
    public void simpleFileAnalysisAddTrim() {
        String data = """
                name,age,percentage
                greg,  12,45.8
                tim,90,22
                """;

        TypeAnalyser analyser = new TypeAnalyser();
        analyser.analyse(data);
    }

    @Test
    public void missingFieldsSetOptionalTest() {
        String data = """
                name,age,percentage
                greg,,45.8
                tim,90,22
                """;

        TypeAnalyser analyser = new TypeAnalyser();
        analyser.defaultValues("""
                columnName,defaultValue
                age,21
                """);
        CsvProcessingConfig csvProcessingConfig = analyser.analyse(data);

        new CsvChecker(csvProcessingConfig).load().stream(data)
                .forEach(System.out::println);
    }
}
