package com.fluxtion.extension;


import com.fluxtion.extension.csvcompiler.ColumnMapping;
import com.fluxtion.extension.csvcompiler.CsvProcessingConfig;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProcessorTest {

    @Test
    @Disabled
    public void loadTest() throws IOException {
        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Royalty");
        csvProcessingConfig.setAcceptPartials(true);
        //columns
        ColumnMapping columnMapping = new ColumnMapping();
        columnMapping.setCsvColumnName("age");
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
        Processor processor = new Processor(csvProcessingConfig);
        RowMarshaller<FieldAccessor> rowMarshaller = processor.load();

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
                latest age,name,registered,resident,town
                48,greg higgins, 34,true,London
                ,bilbo, 105,true,New york
                54,tim higgins, 34,false,Sheffield
                154,Rip Van Winkle, 2,true,Toy town
                """;


        String csvConfig = """
                name: Royalty
                trim: true
                
                columns:
                  ageInYears: {type: int, csvColumnName: 'latest age', optional: true, defaultValue: 50, validationFunction: checkAge}
                  name:
                    defaultValue: testing
                    type: string
                    converterCode:  |
                      String myString = input.toString();
                      return myString.toUpperCase();
                  registered: {type: int}
                  resident: {type: boolean}
                  town: {type: string, converterFunction: toLowerCase}
                
                derivedColumns:
                  nameAndTown:
                    type: string
                    converterCode: return name + "->" + town;
                 
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
                """;

        var summaryStats = Processor.fromYaml(csvConfig).stream(data)
                .filter(r -> r.getField("resident"))
//                .peek(System.out::println)
                .mapToInt(r -> r.getField("ageInYears"))
                .summaryStatistics();

        Assertions.assertEquals(98.0, summaryStats.getSum());
        Assertions.assertEquals(50, summaryStats.getMax());
        Assertions.assertEquals(48, summaryStats.getMin());
        Assertions.assertEquals(2, summaryStats.getCount());

    }
}