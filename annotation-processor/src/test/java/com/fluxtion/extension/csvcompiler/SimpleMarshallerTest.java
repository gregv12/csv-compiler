package com.fluxtion.extension.csvcompiler;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SimpleMarshallerTest {

    @Test
    public void loadPersonMarshallerTest() {
        CsvMarshallerLoader.marshaller(Person.class);
    }

    @Test
    public void validateCsvToNewBeanPerRecordContentsTest() {
        String input = """
                name,age
                tim,32
                lisa,44
                """;

        List<Person> resultList = new ArrayList<>();

        CsvMarshallerLoader.marshaller(Person.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .stream(resultList::add, new StringReader(input));

        assertEquals(2, resultList.size());
        assertIterableEquals(
                List.of(new Person("tim", 32), new Person("lisa", 44)),
                resultList
        );
    }


    @Test
    public void validateCsvToRecycledBeanPerRecordContentsTest() {
        String input = """
                name,age
                tim,32
                lisa,44
                """;

        List<PersonRecycleInstance> resultList = new ArrayList<>();

        CsvMarshallerLoader.marshaller(PersonRecycleInstance.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .stream(resultList::add, new StringReader(input));

        assertEquals(2, resultList.size());
        assertIterableEquals(
                List.of(new PersonRecycleInstance("lisa", 44), new PersonRecycleInstance("lisa", 44)),
                resultList
        );
    }
}
