package com.fluxtion.extension.csvcompiler.example2;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.SingleRowMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

public class SingleMessageParse {

    @Data
    @CsvMarshaller
    public static class Person {
        private String name;
        private int age;
    }

    public static void main(String[] args) {
        SingleRowMarshaller<Person> parser = RowMarshaller.parser(Person.class);
        //headers - no output
        parser.parse("name,age\n");

        System.out.println("parsed:" + parser.parse("Jane,56\n"));
        System.out.println("parsed:" + parser.parse("Isiah,12\n"));
        System.out.println("parsed:" + parser.parse("Sky,42\n"));
    }
}
