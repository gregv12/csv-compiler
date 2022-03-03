package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.beans.Person;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

public class LargeDataTest {
    double sum2 = 0;
    public static int LOOP_COUNT = 3_387;
    public static String FILE_DATA = "./src/test/data/person.txt";

    @EqualsAndHashCode
    @ToString
//@CsvMarshaller
    @CsvMarshaller(formatSource = true)
    public static class PersonDouble {
        private String name;
        private double age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getAge() {
            return age;
        }

        public void setAge(double age) {
            this.age = age;
        }
    }

    @Test
    public void largeDateStreamFileTest() throws IOException {
        RowMarshaller<PersonDouble> personRowMarshaller = RowMarshaller.load(PersonDouble.class);
        personRowMarshaller.stream(new FileReader(FILE_DATA))
                .mapToDouble(PersonDouble::getAge)
                .forEach(i -> sum2 += i);
        double fisrtSum = sum2;
        sum2 = 0;

        Files.lines(new File(FILE_DATA).toPath())
                .filter(s -> !s.startsWith("name"))
                .map(s -> s.split(",")[1])
                .mapToDouble(Double::parseDouble)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, fisrtSum, "csv marshalling problem");
    }

    @Test
    public void largeDateStreamWithAvalidatorFileTest() throws IOException {
        RowMarshaller<PersonDouble> personRowMarshaller = RowMarshaller.load(PersonDouble.class);
        personRowMarshaller.setRowValidator((s, v) -> {})
                .stream(new FileReader(FILE_DATA))
                .mapToDouble(PersonDouble::getAge)
                .forEach(i -> sum2 += i);
        double fisrtSum = sum2;
        sum2 = 0;

        Files.lines(new File(FILE_DATA).toPath())
                .filter(s -> !s.startsWith("name"))
                .map(s -> s.split(",")[1])
                .mapToDouble(Double::parseDouble)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, fisrtSum, "csv marshalling problem");
    }

    @Test
    public void largeDateForEachWithValidatorFileTest() throws IOException {
        RowMarshaller<PersonDouble> personRowMarshaller = RowMarshaller.load(PersonDouble.class);
        personRowMarshaller.setRowValidator((s, v) -> {})
                .forEach(i -> sum2 += i.getAge(), new FileReader(FILE_DATA));
        double fisrtSum = sum2;
        sum2 = 0;

        Files.lines(new File(FILE_DATA).toPath())
                .filter(s -> !s.startsWith("name"))
                .map(s -> s.split(",")[1])
                .mapToDouble(Double::parseDouble)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, fisrtSum, "csv marshalling problem");
    }


    @Test
    public void largeDateForEachFileTest() throws IOException {
        RowMarshaller<PersonDouble> personRowMarshaller = RowMarshaller.load(PersonDouble.class);
        personRowMarshaller.forEach(i -> sum2 += i.getAge(), new FileReader(FILE_DATA));
        double fisrtSum = sum2;
        sum2 = 0;

        Files.lines(new File(FILE_DATA).toPath())
                .filter(s -> !s.startsWith("name"))
                .map(s -> s.split(",")[1])
                .mapToDouble(Double::parseDouble)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, fisrtSum, "csv marshalling problem");
    }

    @Test
    public void largeDateStreamTest() throws IOException {
        RowMarshaller<Person> personRowMarshaller = RowMarshaller.load(Person.class);
        Person person = new Person("greg", 0);
        long sum = 0;
        sum2 = 0;
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);

        for (int i = 0; i < LOOP_COUNT; i++) {
            person.setAge(i);
            personRowMarshaller.writeRow(person, writer);
            sum += i;
        }

        personRowMarshaller.stream(new StringReader(writer.toString()))
                .mapToInt(Person::getAge)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, sum, "csv marshalling problem");
    }

    @Test
    public void largeDateStreamWithValidatorTest() throws IOException {
        RowMarshaller<Person> personRowMarshaller = RowMarshaller.load(Person.class);
        Person person = new Person("greg", 0);
        long sum = 0;
        sum2 = 0;
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);

        for (int i = 0; i < LOOP_COUNT; i++) {
            person.setAge(i);
            personRowMarshaller.writeRow(person, writer);
            sum += i;
        }

        personRowMarshaller.setRowValidator((s, v) -> {}).stream(new StringReader(writer.toString()))
                .mapToInt(Person::getAge)
                .forEach(i -> sum2 += i);

        Assertions.assertEquals(sum2, sum, "csv marshalling problem");
    }

    @Test
    public void largeDateForEachTest() throws IOException {
        RowMarshaller<Person> personRowMarshaller = RowMarshaller.load(Person.class);
        Person person = new Person("greg", 0);
        long sum = 0;
        sum2 = 0;
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);

        for (int i = 0; i < LOOP_COUNT; i++) {
            person.setAge(i);
            personRowMarshaller.writeRow(person, writer);
            sum += i;
        }

        personRowMarshaller.forEach(p -> sum2 += p.getAge(), new StringReader(writer.toString()));
        Assertions.assertEquals(sum2, sum, "csv marshalling problem");
    }


    @Test
    public void largeDateForEachWithValidatorTest() throws IOException {
        RowMarshaller<Person> personRowMarshaller = RowMarshaller.load(Person.class);
        Person person = new Person("greg", 0);
        long sum = 0;
        sum2 = 0;
        StringWriter writer = new StringWriter();
        personRowMarshaller.writeHeaders(writer);

        for (int i = 0; i < LOOP_COUNT; i++) {
            person.setAge(i);
            personRowMarshaller.writeRow(person, writer);
            sum += i;
        }

        personRowMarshaller.setRowValidator((s, v) -> {})
                .forEach(p -> sum2 += p.getAge(), new StringReader(writer.toString()));
        Assertions.assertEquals(sum2, sum, "csv marshalling problem");
    }

}
