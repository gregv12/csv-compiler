package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.CsvMarshallerLoader;
import com.fluxtion.extension.csvcompiler.ValidationLogger;

import java.io.StringReader;

public class Main {

    public static void main(String[] args) {
        CsvMarshallerLoader.marshaller(Person.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .stream(System.out::println, new StringReader("name,age\n" +
                                                              "Linda Smith,43\n" +
                                                              "Soren Miller,33\n" +
                                                              "fred,not a number\n"
                ));
    }
}
