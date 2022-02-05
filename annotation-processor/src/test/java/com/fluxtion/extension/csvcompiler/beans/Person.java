/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.PostProcessMethod;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Supplier;

@EqualsAndHashCode
@ToString
//@CsvMarshaller
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

    @CsvMarshaller(formatSource = true)
    public static class MapColumn extends Person{

        @ColumnMapping(columnName = "overrideNameMapping")
        private String name;
    }

    @CsvMarshaller(formatSource = true)
    public static class DefaultColumnValue extends Person{

        @ColumnMapping(defaultValue = "NO NAME")
        private String name;

        @ColumnMapping(defaultValue = "18")
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class NoTrimField extends Person{
        @ColumnMapping(trimOverride = true)
        private String name;
    }

    @CsvMarshaller(formatSource = true)
    public static class OptionalField extends Person{
        @ColumnMapping(optionalField = true)
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class OptionalFieldWithDefaultValue extends Person{
        @ColumnMapping(optionalField = true, defaultValue = "18")
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class PostProcess extends Person{

        @PostProcessMethod
        public void myPostProcessMethod(){
            setName(getName().toUpperCase());
        }
    }
}
