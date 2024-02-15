package com.fluxtion.extension.csvcompiler.example1;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.ValidationLogger;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

/**
 * Simple example demonstrating processing a Csv records with a Java bean representing each record. Running the application
 * will print to standard out:
 *
 * <pre>
 * StreamBeans.Person(name=Linda Smith, age=43)
 * StreamBeans.Person(name=Soren Miller, age=33)
 * Person problem pushing 'not a number' from row:'4' fieldIndex:'1' targetMethod:'Person#setAge' error:'java.lang.NumberFormatException: not a number'
 *
 * RESULT - Max age:43
 * </pre>
 */
public class StreamBeans {

    @Data
    @CsvMarshaller
    public static class Person {
        private String name;
        private int age;
    }

    public static void main(String[] args) {
        RowMarshaller.load(Person.class)
                .setValidationLogger(ValidationLogger.CONSOLE)
                .stream("name,age\n" +
                        "Linda Smith,43\n" +
                        "Soren Miller,33\n" +
                        "fred,not a number\n")
                .peek(System.out::println)
                .mapToInt(Person::getAge)
                .max()
                .ifPresent(i -> System.out.println("\nRESULT - Max age:" + i));
    }
}
