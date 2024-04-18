package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SingleParserTest {


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @CsvMarshaller
    public static class Person {
        private String name;
        private int age;
    }

    @Test
    public void testSingleParse() {
        SingleRowMarshaller<Person> parser = RowMarshaller.parser(Person.class);
        //headers - no output
        parser.parse("name,age\n");

        Assertions.assertEquals(new Person("Jane", 56), parser.parse("Jane,56\n"));
        Assertions.assertEquals(new Person("Isiah", 12), parser.parse("Isiah,12\n"));
        Assertions.assertEquals(new Person("Sky", 42), parser.parse("Sky,42\n"));
    }
}
