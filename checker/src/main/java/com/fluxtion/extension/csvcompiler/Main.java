package com.fluxtion.extension.csvcompiler;

import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static com.fluxtion.extension.csvcompiler.Version.VERSION;

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
    public static final String SAMPLE_DATA = """
            age,name,registered
            52,greg higgins, 34
            48,tim higgins, 34
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
    @Option(names = {"-s", "--simpleConfig"}, description = "prints a minimal sample yaml config", arity = "0")
    boolean printSample;
    @Option(names = {"-f", "--fullConfig"}, description = "prints a sample yaml config showing all options", arity = "0")
    boolean printSampleFull;
    @Option(names = {"-d", "--sampleData"}, description = "prints data for sample config", arity = "0")
    boolean printSampleData;

    @Option(names = {"-l", "--lookupFile"}, description = "lookup csv file use multiple times top specify more than one lookup file")
    List<String> lookupFiles;
    @Parameters(paramLabel = "<check config>", defaultValue = "processConfig.yaml", index = "0",
            description = "Configuration of csv check logic")
    private File configFile;
    @Parameters(paramLabel = "<input data>", defaultValue = "data.csv", description = "input data file list as space separated list", index = "1..*")
    private List<File> dataFiles;
    private String currentDataFile = "";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @SneakyThrows
    @Override
    public void run() {
        System.out.println("lookup:" + lookupFiles);
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

        Path resultsDir = Paths.get("results");
        if (!Files.exists(resultsDir)) {
            Files.createDirectory(resultsDir);
        }
        File validResultFile = new File("results/valid.csv");
        validResultFile.delete();
        File invalidResultFile = new File("results/invalid.txt");
        invalidResultFile.delete();
        Writer writer = new BufferedWriter(new FileWriter(validResultFile));
        Writer writerInvalid = new BufferedWriter(new FileWriter(invalidResultFile));
        LongAdder invalidCount = new LongAdder();
        LongAdder validCount = new LongAdder();
        var metaMap = new HashMap<String, String>();
        metaMap.put("configFile", configFile.getName());

        RowMarshaller<FieldAccessor> rowMarshaller = CsvChecker.fromYaml(new FileReader(configFile));
        rowMarshaller.writeHeaders(writer);
        rowMarshaller
                .addLookup("meta", metaMap::get)
                .setValidationLogger(new ValidationLogger() {
                    @Override
                    public void logFatal(CsvProcessingException csvProcessingException) {
                        invalidCount.increment();
                        Main.this.write(writerInvalid,csvProcessingException.getMessage());
                    }

                    @Override
                    public void logWarning(CsvProcessingException csvProcessingException) {
                        invalidCount.increment();
                        Main.this.write(writerInvalid,csvProcessingException.getMessage());
                    }
                });
        loadLookup(rowMarshaller);
        for (File file : dataFiles) {
            metaMap.put("dataFile", file.getName());
            processFile(rowMarshaller, file, writer, validCount);
        }
        writer.flush();
        writerInvalid.flush();
        System.out.println("Valid count  : " + validCount.intValue());
        System.out.println("Invalid count: " + invalidCount.intValue());
    }

    @SneakyThrows
    private void loadLookup(RowMarshaller<FieldAccessor> rowMarshaller) {
        Map<String, Map<String, String>> lookupMultiMap = new HashMap<>();
        for (String lookupFile : lookupFiles) {
            try(FileReader fileReader = new FileReader(lookupFile)){
                RowMarshaller.load(LookupTable.class)
                        .stream(fileReader)
                        .forEach(l ->{
                            lookupMultiMap.computeIfAbsent(l.getTable(), s -> new HashMap<>()).put(l.getKey(), l.getValue());
                        });
            }
        }
        lookupMultiMap.forEach((t, m) ->{
            rowMarshaller.addLookup(t, m::get);
        });
    }

    @SneakyThrows
    private void processFile(RowMarshaller<FieldAccessor> rowMarshaller, File dataFile, Writer writer, LongAdder validCount ){
        currentDataFile = dataFile.getAbsolutePath();
        System.out.println("data  : " + currentDataFile);
        rowMarshaller.stream(new FileReader(dataFile))
                .forEach(r -> {
                    try {
                        validCount.increment();
                        rowMarshaller.writeRow(r, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SneakyThrows
    private void write(Writer writer, String message) {
        writer.write(message);
        writer.write('\n');
        writer.write("file:" + currentDataFile);
        writer.write('\n');
    }

    private void printSample() {
        System.out.println("""
                sample yaml, showing minimal fields:
                --------------------------------------
                """ + SAMPLE_CONFIG);
    }

    private void printSampleFull() {

        CsvProcessingConfig csvProcessingConfig = new CsvProcessingConfig();
        csvProcessingConfig.setName("Royalty");
        csvProcessingConfig.setAcceptPartials(true);
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

        Yaml yaml = new Yaml();
        yaml.dumpAsMap(csvProcessingConfig);

        System.out.println("""
                sample yaml, showing all fields:
                -----------------------------------
                """ + yaml.dumpAsMap(csvProcessingConfig));
    }

    private void printSampleData() {
        System.out.println("""
                sample data matched to sample config:
                --------------------------------------
                """ + SAMPLE_DATA);
    }
}