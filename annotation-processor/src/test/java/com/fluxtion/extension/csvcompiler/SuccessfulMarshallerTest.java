package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.AllNativeMarshallerTypes;
import com.fluxtion.extension.csvcompiler.beans.Person;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SuccessfulMarshallerTest {

    @Test
    public void loadPersonMarshallerTest() {
        CsvMarshallerLoader.marshaller(Person.class);
    }

    @Test
    public void validateCsvToNewBeanPerRecordContentsTest() {
        testPerson(
                Person.class,
                "name,age\n" +
                "tim,32\n" +
                "\n" +
                "lisa,44\n",
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void validateCanSkipEmptyLines() {
        testPerson(
                Person.class,
                "name,age\n" +
                "tim,32\n" +
                "\n" +
                "lisa,44\n",
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void validateCsvToRecycledBeanPerRecordContentsTest() {
        testPerson(
                Person.PersonRecycleInstance.class,
                "name,age\n" +
                "tim,32\n" +
                "lisa,44\n",
                Person.build(Person.PersonRecycleInstance::new, "lisa", 44),
                Person.build(Person.PersonRecycleInstance::new, "lisa", 44)
        );
    }

    @Test
    public void noSkipPerson_NoSkipCsvMarshallerEmptyLines() {
        testPerson(
                Person.NoSkip.class,
                "name,age\n" +
                "tim,32\n" +
                "\n" +
                "lisa,44\n",
                Person.build(Person.NoSkip::new, "tim", 32),
                Person.build(Person.NoSkip::new, "lisa", 44)
        );
    }

    @Test
    public void allDefaultPropertyTypeMarshaller() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                       "true,8,10.7,1.5,100,2000,4,hello\n";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        CsvMarshallerLoader.marshaller(AllNativeMarshallerTypes.class).stream(resultList::add, new StringReader(input));

        AllNativeMarshallerTypes bean = new AllNativeMarshallerTypes();
        bean.setBooleanProperty(true);
        bean.setByteProperty((byte) 8);
        bean.setDoubleProperty(10.7);
        bean.setFloatProperty(1.5f);
        bean.setIntProperty(100);
        bean.setLongProperty(2000);
        bean.setShortProperty((short) 4);
        bean.setStringProperty("hello");

        assertIterableEquals(
                List.of(bean),
                resultList
        );
    }

    @SafeVarargs
    private static <T extends Person> void testPerson(Class<T> personClass, String input, T... people) {
        List<? super Person> resultList = new ArrayList<>();
        CsvMarshallerLoader.marshaller(personClass).stream(resultList::add, new StringReader(input));
        assertIterableEquals(
                List.of(people),
                resultList
        );

    }
}
