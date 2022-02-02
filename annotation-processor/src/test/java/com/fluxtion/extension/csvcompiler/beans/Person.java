package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Supplier;

@EqualsAndHashCode
@ToString
@CsvMarshaller(formatSource = true)
public class Person {

    public static <T extends Person> T build(Supplier<T> personSupplier, String name, int age) {
        T subPerson = personSupplier.get();
        subPerson.setAge(age);
        subPerson.setName(name);
        return subPerson;
    }

    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person() {
    }

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

    @CsvMarshaller(skipEmptyLines = false, formatSource = true)
    public static class NoSkip extends Person {
    }

    @CsvMarshaller(headerLines = 3, mappingRow = 2, formatSource = true)
    public static class MultipleHeaderLines extends Person {
    }

    @CsvMarshaller(formatSource = true, newBeanPerRecord = false)
    public static class PersonRecycleInstance extends Person{
    }

    @CsvMarshaller(formatSource = true, processEscapeSequences = true)
    public static class Escaped extends Person{
    }

    @CsvMarshaller(formatSource = true, fieldSeparator = '|')
    public static class PipeSeparator extends Person{
    }

    @CsvMarshaller(formatSource = true, acceptPartials = true)
    public static class AcceptPartials extends Person{
    }

    @CsvMarshaller(formatSource = true, trim = true)
    public static class Trim extends Person{
    }

    @CsvMarshaller(formatSource = true, failOnFirstError = true)
    public static class FailFast extends Person{
    }

    @CsvMarshaller(formatSource = true, ignoredChar = '\0', lineEnding = '\r')
    public static class UnixLineEnding extends Person{
    }
}
