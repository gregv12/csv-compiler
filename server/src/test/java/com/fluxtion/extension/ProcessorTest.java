package com.fluxtion.extension;


import com.fluxtion.extension.csvcompiler.ColumnMapping;
import com.fluxtion.extension.csvcompiler.CsvProcessingConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProcessorTest {

    @Test
    public void loadTest() throws IOException {
        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Royalty");
        csvProcessingConfig.setAcceptPartials(true);
        //columns
        ColumnMapping columnMapping = new ColumnMapping();
        columnMapping.setName("ageInYears");
        columnMapping.setCsvColumnName("age");
        columnMapping.setType("int");
        columnMapping.setOptional(true);
        csvProcessingConfig.getColumnMap().put(columnMapping.getName(), columnMapping);
        //
        columnMapping = new ColumnMapping();
        columnMapping.setName("name");
        columnMapping.setType("java.lang.String");
        columnMapping.setDefaultValue("testing");
        csvProcessingConfig.getColumnMap().put(columnMapping.getName(), columnMapping);
        //
        columnMapping = new ColumnMapping();
        columnMapping.setName("registered");
        columnMapping.setType("int");
        csvProcessingConfig.getColumnMap().put(columnMapping.getName(), columnMapping);
        //generate
        Processor processor = new Processor(csvProcessingConfig);
        processor.load();
    }
}