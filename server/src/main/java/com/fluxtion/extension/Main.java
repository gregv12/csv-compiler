package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.CsvProcessingException;
import com.fluxtion.extension.csvcompiler.FieldAccessor;
import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.ValidationLogger;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.concurrent.atomic.LongAdder;
// some exports omitted for the sake of brevity

@Command(name = "csvCheck", version = "csvCheck", mixinStandardHelpOptions = true)
public class Main implements Runnable {

//    @Option(names = { "-s", "--font-size" }, description = "Font size")
//    int fontSize = 19;

    @Parameters(paramLabel = "<check config>", defaultValue = "processConfig.yaml", index = "0",
            description = "Configuration of csv check logic")
    private File configFile;

    @Parameters(paramLabel = "<csv data>", defaultValue = "data.csv", index = "1",
            description = "csv data file")
    private File dataFile;

    @SneakyThrows
    @Override
    public void run() {
        System.out.println("config: " + configFile.getAbsolutePath());
        System.out.println("data  : " + dataFile.getAbsolutePath());
        new File("valid.csv").delete();
        new File("invalid.txt").delete();
        Writer writer = new BufferedWriter( new FileWriter("valid.csv"));
        Writer writerInvalid = new BufferedWriter( new FileWriter("invalid.txt"));
        LongAdder invalidCount = new LongAdder();
        LongAdder validCount = new LongAdder();

        RowMarshaller<FieldAccessor> rowMarshaller = Processor.fromYaml(new FileReader(configFile));
        rowMarshaller.writeHeaders(writer);
        rowMarshaller
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
    private void write(Writer writer, String message){
        writer.write(message);
        writer.write('\n');
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}