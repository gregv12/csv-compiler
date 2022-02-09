package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.fluxtion.extension.csvcompiler.ValidationLogger;

import java.io.StringReader;
import java.util.ServiceLoader;

import static java.util.ServiceLoader.load;

public class Main {

    public static void main(String[] args) {
        RowMarshaller.load(Person.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .forEach(System.out::println, new StringReader("name,age\n" +
                                                              "Linda Smith,43\n" +
                                                              "Soren Miller,33\n" +
                                                              "fred,not a number\n"
                ));
    }


    static String findConverterClass(String converterId){
        System.out.println("looking for:" + converterId);
        return load(FieldConverter.class).stream()
                .peek(System.out::println)
                .map(ServiceLoader.Provider::get)
                .filter(f -> f.getName().equals(converterId))
                .peek(System.out::println)
                .map(FieldConverter::getClass)
                .map(Class::getCanonicalName)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No converter registered with " +
                        "ServiceLoader under the name:" + converterId));

    }
}
