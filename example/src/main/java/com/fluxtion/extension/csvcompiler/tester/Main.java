package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.ValidationLogger;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.ToString;

public class Main {

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
                .ifPresent(i -> System.out.println("Max age:" + i));
    }

    @ToString
    @CsvMarshaller(formatSource = true)
    public static class Person {

        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
