package com.fluxtion.extension;


import com.fluxtion.extension.csvcompiler.*;
import com.fluxtion.extension.csvcompiler.converters.ConstantStringConverter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

public class CsvCheckerTest {

    @Test
    public void libraryConverterTest(){
        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Royalty");
//        csvProcessingConfig.setAcceptPartials(true);
        csvProcessingConfig.setTrim(true);
        //columns
        ColumnMapping columnMapping = new ColumnMapping();
        columnMapping.setSourceColumnName("age");
        columnMapping.setType("int");
        columnMapping.setOptional(true);
//        columnMapping.setTrimOverride(true);
        csvProcessingConfig.getColumns().put("ageInYears", columnMapping);
        //
        columnMapping = new ColumnMapping();
        columnMapping.setType("java.lang.String");
        columnMapping.setConverter(ConstantStringConverter.ID);
        columnMapping.setConverterConfiguration("TEST");
        csvProcessingConfig.getColumns().put("name", columnMapping);
        //debug
        csvProcessingConfig.setDumpYaml(true);
        csvProcessingConfig.setDumpGeneratedJava(true);
        //generate
        CsvChecker csvChecker = new CsvChecker(csvProcessingConfig);
        RowMarshaller<FieldAccessor> rowMarshaller = csvChecker.load();

        String data = """
                age,name
                52 ,greg
                48,tim
                """;

        int ageSum = rowMarshaller.stream(data)
                .peek(System.out::println)
                .filter(r -> r.getField("name").toString().equals("TEST"))
                .map(r -> r.getField("ageInYears"))
                .mapToInt(Integer.class::cast)
                .sum();

        Assertions.assertEquals(100, ageSum);
        System.out.println("ageSum:" + ageSum);

    }

    @Test
    public void arrayTest(){
        String data = """
                ageInYears,resident
                48|34|56        ,true
                          ,true
                54        ,false
                154       ,true
                36        ,true
                """;
        String csvConfig = """
                name: Royalty
                trim: true
                dumpGeneratedJava: true
                                
                columns:
                  #ageInYears: {type: "int[]"}
                  ageInYearsList: {type: "List<int>", sourceColumnName: ageInYears}
                  resident: {type: boolean}
                """;
        StringBuilder sb = new StringBuilder();
        RowMarshaller<FieldAccessor> rowMarshaller = CsvChecker.fromYaml(csvConfig);
        rowMarshaller.writeHeaders(sb);
        rowMarshaller.stream(data).forEach(r -> rowMarshaller.writeRow(r, sb));
        System.out.println(sb);
    }


    @Test
//    @Disabled
    public void loadTest() throws IOException {
        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Royalty");
        csvProcessingConfig.setAcceptPartials(true);
//        csvProcessingConfig.setTrim(true);
        //columns
        ColumnMapping columnMapping = new ColumnMapping();
        columnMapping.setSourceColumnName("age");
        columnMapping.setType("int");
        columnMapping.setOptional(true);
        csvProcessingConfig.getColumns().put("ageInYears", columnMapping);
        //
        columnMapping = new ColumnMapping();
        columnMapping.setType("java.lang.String");
        columnMapping.setDefaultValue("testing");
        csvProcessingConfig.getColumns().put("name", columnMapping);
        //
        columnMapping = new ColumnMapping();
        columnMapping.setType("int");
        columnMapping.setTrimOverride(true);
        csvProcessingConfig.getColumns().put("registered", columnMapping);
        //generate
        CsvChecker csvChecker = new CsvChecker(csvProcessingConfig);
        RowMarshaller<FieldAccessor> rowMarshaller = csvChecker.load();

        String data = """
                age,name,registered
                52,greg higgins, 34
                48,tim higgins, 34
                """;

        int ageSum = rowMarshaller.stream(data)
                .map(r -> r.getField("ageInYears"))
                .mapToInt(Integer.class::cast)
                .sum();

        System.out.println("ageSum:" + ageSum);
    }

    @SneakyThrows
    @Test
    public void loadFromYamlTest() {
        String data = """
                latest age,name          ,registered     ,resident,town
                48        ,greg higgins  , registered    ,true    ,London
                          ,bilbo         , registered    ,true    ,New york
                54        ,tim higgins   ,               ,false   ,Sheffield
                154       ,Rip Van Winkle, unregistered  ,true    ,Toy town
                36        ,jack dempsey  , xsoihf        ,true    ,Chicago
                """;
        String csvConfig = """
                name: Royalty
                trim: true
                #dumpGeneratedJava: true
                                
                columns:
                  ageInYears: {type: int, sourceColumnName: 'latest age', optional: true, defaultValue: 50, validationFunction: checkAge}
                  name:
                    defaultValue: testing
                    type: string
                    converterCode:  |
                      String myString = input.toString();
                      return myString.toUpperCase();
                  registered: {type: int, lookupTable: registeredId, defaultValue: unknown, validationFunction: checkRegistered}
                  resident: {type: boolean}
                  town: {type: string, converterFunction: toLowerCase}
                                
                derivedColumns:
                  nameAndTown:
                    type: string
                    converterCode: return name + "->" + town;
                  dataFile: {type: String, lookupTable: meta, defaultValue: dataFile}
                 
                conversionFunctions:
                  toLowerCase:
                    convertsTo: string
                    code:  |
                      String myString = input.toString();
                      return myString.toLowerCase();
                      
                validationFunctions:
                  checkAge:
                    code: |
                      if(ageInYears > 100){
                        validationLog.accept(ageInYears +  " way too old!!", false);
                        return false;
                      }
                      return true;
                      
                  checkRegistered:
                    code: |
                      if(registered > 4){
                        validationLog.accept("Unsupported registration description", false);
                        return false;
                      }
                      return true;
                      
                lookupTables:
                  registeredId:
                    registered: 1
                    unregistered: 2
                    waiting: 3
                    unknown: 4
                    default: 5
                """;

        RowMarshaller<FieldAccessor> rowMarshaller = CsvChecker.fromYaml(csvConfig);
        StringWriter successWriter = new StringWriter();
        rowMarshaller.writeHeaders(successWriter);
        var metaMap = new HashMap<String, String>();
        metaMap.put("dataFile", "inMemoryData");
        metaMap.put("configFile", "inMemoryConfig");
        var summaryStats = rowMarshaller
                .addLookup("meta", metaMap::get)
                .stream(data)
                .filter(r -> r.getField("resident"))
                .peek(System.out::println)
                .peek(r -> {
                    try {
                        rowMarshaller.writeRow(r, successWriter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapToInt(r -> r.getField("ageInYears"))
                .summaryStatistics();

        Assertions.assertEquals(98.0, summaryStats.getSum());
        Assertions.assertEquals(50, summaryStats.getMax());
        Assertions.assertEquals(48, summaryStats.getMin());
        Assertions.assertEquals(2, summaryStats.getCount());

        System.out.println("Valid:\n" + successWriter.toString());
    }
}