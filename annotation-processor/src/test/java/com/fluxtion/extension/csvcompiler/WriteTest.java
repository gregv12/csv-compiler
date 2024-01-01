package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.processor.Util;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class WriteTest {

    @Test
    public void writeReadTest() throws IOException {
        RowMarshaller<Person> personRowMarshaller = RowMarshaller.load(Person.class);
        Person person = new Person("greg", 55);
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);
        personRowMarshaller.writeRow(person, writer);

        List<Person> results = personRowMarshaller.stream(new StringReader(writer.toString()))
                .collect(Collectors.toList());

        Assertions.assertIterableEquals(
                Util.listOf(person),
                results
        );
    }

    @Test
    public void writeNoOutputFieldTest() throws IOException {
        RowMarshaller<Data> personRowMarshaller = RowMarshaller.load(Data.class);
        Data person = new Data("greg", 55);
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);
        Assertions.assertEquals("name", writer.toString().trim());

        writer.getBuffer().setLength(0);
        personRowMarshaller.writeInputHeaders(writer);
        Assertions.assertEquals("name,age", writer.toString().trim());

        writer.getBuffer().setLength(0);
        personRowMarshaller.writeRow(person, writer);
        Assertions.assertEquals("greg", writer.toString().trim());
    }


    @CsvMarshaller
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private String name;
        @ColumnMapping(outputField = false)
        private int age;
    }
}
