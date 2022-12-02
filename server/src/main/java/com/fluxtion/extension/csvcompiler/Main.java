package com.fluxtion.extension.csvcompiler;

import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static com.fluxtion.extension.csvcompiler.Version.VERSION;
// some exports omitted for the sake of brevity

@Command(name = "csvCheck", version = VERSION, mixinStandardHelpOptions = true)
public class Main implements Runnable {

    public static final String FULL_SAMPLE_CONFIG = """
            name: Royalty
            mappingRow: 1
            processEscapeSequences: false
            skipCommentLines: true
            skipEmptyLines: false
            trim: true
            failOnFirstError: false
            fieldSeparator: ','
            headerLines: 0
            ignoreQuotes: false
            acceptPartials: false
                        
            columns:
              ageInYears:
                converterCode: ''
                converterFunction: ''
                csvColumnName: latest age
                csvIndex: -1
                defaultValue: '50'
                derived: false
                lookupTable: null
                name: null
                optional: true
                trimOverride: false
                type: int
                validationFunction: checkAge
              name:
                converterCode: |
                  String myString = input.toString();
                  return myString.toUpperCase();
                converterFunction: ''
                csvColumnName: ''
                csvIndex: -1
                defaultValue: testing
                derived: false
                lookupTable: null
                name: null
                optional: false
                trimOverride: false
                type: string
                validationFunction: ''
              registered:
                converterCode: ''
                converterFunction: ''
                csvColumnName: ''
                csvIndex: -1
                defaultValue: unknown
                derived: false
                lookupTable: registeredId
                name: null
                optional: false
                trimOverride: false
                type: int
                validationFunction: checkRegistered
              resident:
                converterCode: ''
                converterFunction: ''
                csvColumnName: ''
                csvIndex: -1
                defaultValue: ''
                derived: false
                lookupTable: null
                name: null
                optional: false
                trimOverride: false
                type: boolean
                validationFunction: ''
              town:
                converterCode: ''
                converterFunction: toLowerCase
                csvColumnName: ''
                csvIndex: -1
                defaultValue: ''
                derived: false
                lookupTable: null
                name: null
                optional: false
                trimOverride: false
                type: string
                validationFunction: ''
                
            derivedColumns:
              nameAndTown:
                converterCode: return name + "->" + town;
                converterFunction: ''
                csvColumnName: ''
                csvIndex: -1
                defaultValue: ''
                derived: false
                lookupTable: null
                name: null
                optional: false
                trimOverride: false
                type: string
                validationFunction: ''
                
            conversionFunctions:
              toLowerCase:
                code: |
                  String myString = input.toString();
                  return myString.toLowerCase();
                convertsTo: string
                name: null

            validationFunctions:
              checkAge:
                code: |
                  if(ageInYears > 100){
                    validationLog.accept(ageInYears +  " way too old!!", false);
                    return false;
                  }
                  return true;
                name: null
              checkRegistered:
                code: |
                  if(registered > 4){
                    validationLog.accept("Unsupported registration description", false);
                    return false;
                  }
                  return true;
                name: null
                        
            lookupTables:
              registeredId:
                registered: 1
                unregistered: 2
                waiting: 3
                unknown: 4
                default: 5
            """;
    private static String SAMPLE_CONFIG = """
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

    public static final String SAMPLE_DATA = """
            age,name,registered
            52,greg higgins, 34
            48,tim higgins, 34
            """;
    @Option(names = {"-s", "--simpleConfig"}, description = "prints a minimal sample yaml config", arity = "0")
    boolean printSample;
    @Option(names = {"-f", "--fullConfig"}, description = "prints a sample yaml config showing all options", arity = "0")
    boolean printSampleFull;
    @Option(names = {"-d", "--sampleData"}, description = "prints data for sample config", arity = "0")
    boolean printSampleData;
    @Parameters(paramLabel = "<check config>", defaultValue = "processConfig.yaml", index = "0",
            description = "Configuration of csv check logic")
    private File configFile;
    @Parameters(paramLabel = "<csv data>", defaultValue = "data.csv", index = "1",
            description = "csv data file")
    private File dataFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @SneakyThrows
    @Override
    public void run() {
        if (printSample) {
            printSample();
        } else if (printSampleFull) {
            printSampleFull();
        } else if (printSampleData) {
            printSampleData();
        } else {
            process();
        }
    }

    @SneakyThrows
    private void process() {
        System.out.println("config: " + configFile.getAbsolutePath());
        System.out.println("data  : " + dataFile.getAbsolutePath());
        new File("valid.csv").delete();
        new File("invalid.txt").delete();
        Writer writer = new BufferedWriter(new FileWriter("valid.csv"));
        Writer writerInvalid = new BufferedWriter(new FileWriter("invalid.txt"));
        LongAdder invalidCount = new LongAdder();
        LongAdder validCount = new LongAdder();
        var metaMap = new HashMap<String, String>();
        metaMap.put("dataFile", dataFile.getName());
        metaMap.put("configFile", configFile.getName());

        RowMarshaller<FieldAccessor> rowMarshaller = CsvChecker.fromYaml(new FileReader(configFile));
        rowMarshaller.writeHeaders(writer);
        rowMarshaller
                .addLookup("meta", metaMap::get)
                .setValidationLogger(new ValidationLogger() {
                    @Override
                    public void logFatal(CsvProcessingException csvProcessingException) {
                        invalidCount.increment();
                        Main.this.write(writerInvalid, csvProcessingException.getMessage());
                    }

                    @Override
                    public void logWarning(CsvProcessingException csvProcessingException) {
                        invalidCount.increment();
                        Main.this.write(writerInvalid, csvProcessingException.getMessage());
                    }
                })
                .stream(new FileReader(dataFile))
                .forEach(r -> {
                    try {
                        validCount.increment();
                        rowMarshaller.writeRow(r, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        writer.flush();
        writerInvalid.flush();
        System.out.println("Valid count  : " + validCount.intValue());
        System.out.println("Invalid count: " + invalidCount.intValue());
    }

    @SneakyThrows
    private void write(Writer writer, String message) {
        writer.write(message);
        writer.write('\n');
    }

    private void printSample() {
        System.out.println("""
                sample yaml, showing minimal fields:
                --------------------------------------
                """ + SAMPLE_CONFIG);
    }

    private void printSampleFull() {
        System.out.println("""
                sample yaml, showing all fields:
                -----------------------------------
                """ + FULL_SAMPLE_CONFIG);
    }

    private void printSampleData() {
        System.out.println("""
                sample data matched to sample config:
                --------------------------------------
                """ + SAMPLE_DATA);
    }
}