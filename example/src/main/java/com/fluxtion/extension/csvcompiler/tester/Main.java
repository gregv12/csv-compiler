package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.CsvMarshallerLoader;
import com.fluxtion.extension.csvcompiler.ValidationLogger;

import java.io.StringReader;

public class Main {

    public static void main(String[] args) {
        CsvMarshallerLoader.marshaller(Person.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .stream(System.out::println, new StringReader("""
                        name,age
                        Linda Smith,43
                        Soren Miller,33
                        fred,not a number
                        """
                ));
    }
}
