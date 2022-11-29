package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.processor.Util;
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
}
