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

import com.fluxtion.extension.csvcompiler.annotations.*;
import com.fluxtion.extension.csvcompiler.converters.ConstantStringConverter;
import com.fluxtion.extension.csvcompiler.converters.LocalTimeConverter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalTime;
import java.util.function.BiConsumer;
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

    @CsvMarshaller(skipEmptyLines = true, formatSource = true)
    public static class SkipEmptyLines extends Person {
    }

    @CsvMarshaller(headerLines = 3, mappingRow = 2, formatSource = true)
    public static class MultipleHeaderLines extends Person {
    }

    @CsvMarshaller(formatSource = true, newBeanPerRecord = false)
    public static class PersonRecycleInstance extends Person {
    }

    @CsvMarshaller(formatSource = true, processEscapeSequences = true)
    public static class Escaped extends Person {
    }

    @CsvMarshaller(formatSource = true, noHeader = true, processEscapeSequences = true)
    public static class EscapedIndexFields extends Person {

        @ColumnMapping(columnIndex = 0)
        private String name;

        @ColumnMapping(columnIndex = 1)
        private int age;
    }

    @CsvMarshaller(formatSource = true, fieldSeparator = '|')
    public static class PipeSeparator extends Person {
    }

    @CsvMarshaller(formatSource = true, acceptPartials = true)
    public static class AcceptPartials extends Person {
    }

    @CsvMarshaller(formatSource = true, trim = true)
    public static class Trim extends Person {
    }

    @CsvMarshaller(formatSource = true, failOnFirstError = true)
    public static class FailFast extends Person {
    }

    @CsvMarshaller(formatSource = true)
    public static class LegacyMacLineEnding extends Person {
    }

    @CsvMarshaller(formatSource = true)
    public static class MapColumn extends Person {

        @ColumnMapping(columnName = "overrideNameMapping")
        private String name;
    }

    @CsvMarshaller(formatSource = true)
    public static class DefaultColumnValue extends Person {

        @ColumnMapping(defaultValue = "NO NAME")
        private String name;

        @ColumnMapping(defaultValue = "18")
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class NoTrimField extends Person {
        @ColumnMapping(trimOverride = true)
        private String name;
    }

    @CsvMarshaller(formatSource = true)
    public static class OptionalField extends Person {
        @ColumnMapping(optionalField = true)
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class OptionalFieldWithDefaultValue extends Person {
        @ColumnMapping(optionalField = true, defaultValue = "18")
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class PostProcess extends Person {

        @PostProcessMethod
        public void myPostProcessMethod() {
            setName(getName().toUpperCase());
        }
    }

    @CsvMarshaller(formatSource = true, noHeader = true)
    public static class IndexFields extends Person {

        @ColumnMapping(columnIndex = 0)
        private String name;

        @ColumnMapping(columnIndex = 1)
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class ConverterField extends Person {

        @DataMapping(converter = LocalTimeConverter.class, configuration = "HH:mm")
        private LocalTime birthTime;

        @DataMapping(converter = ConstantStringConverter.class, configuration = "ALWAYS_LINDA")
        private String name;

        public LocalTime getBirthTime() {
            return birthTime;
        }

        public void setBirthTime(LocalTime birthTime) {
            this.birthTime = birthTime;
        }
    }

    @CsvMarshaller(formatSource = true)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class ConverterFieldLocalMethod extends Person {

        @DataMapping(converter = LocalTimeConverter.class, configuration = "HH:mm")
        private LocalTime birthTime;

        @DataMapping(conversionMethod = "alwaysLinda")
        private String name;

        public LocalTime getBirthTime() {
            return birthTime;
        }

        public void setBirthTime(LocalTime birthTime) {
            this.birthTime = birthTime;
        }

        public String alwaysLinda(CharSequence charSequence){
            return "ALWAYS_LINDA";
        }
    }

    @CsvMarshaller(formatSource = true)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class DerivedFieldLocalMethod extends Person {

        @DataMapping(converter = LocalTimeConverter.class, configuration = "HH:mm")
        private LocalTime birthTime;

        @DataMapping(conversionMethod = "checkAllSet", derivedColumn = true)
        private boolean derivedName = false;

        public LocalTime getBirthTime() {
            return birthTime;
        }

        public void setBirthTime(LocalTime birthTime) {
            this.birthTime = birthTime;
        }

        public boolean isDerivedName() {
            return derivedName;
        }

        public void setDerivedName(boolean derivedName) {
            this.derivedName = derivedName;
        }

        public boolean checkAllSet(CharSequence charSequence){
            return birthTime!=null && getName()!=null && getAge() > 0;
        }
    }

    @CsvMarshaller(formatSource = true, loopAssignmentLimit = 0)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class ConverterFieldLoopAssignment extends Person {

        @DataMapping(converter = LocalTimeConverter.class, configuration = "HH:mm")
        private LocalTime birthTime;

        @DataMapping(converter = ConstantStringConverter.class, configuration = "ALWAYS_LINDA")
        private String name;

        public LocalTime getBirthTime() {
            return birthTime;
        }

        public void setBirthTime(LocalTime birthTime) {
            this.birthTime = birthTime;
        }
    }

    @CsvMarshaller(formatSource = true)
    public static class Lookup extends Person {

        public static final String AGE_LOOKUP = "age";

        @DataMapping(lookupName = AGE_LOOKUP)
        private int age;
    }

    @CsvMarshaller(formatSource = true)
    public static class LookupDefaultValue extends Person {

        public static final String NAME_LOOKUP = "name";

        @ColumnMapping(defaultValue = "tom")
        @DataMapping(lookupName = NAME_LOOKUP, derivedColumn = true)
        private String name;
    }


    @CsvMarshaller(formatSource = true)
    public static class Validation extends Person {

        @Validator(validationLambda = "(int age) -> age > 0", errorMessage = "age must be greater 0", exitOnFailure = false)
        private int age;

    }

    @CsvMarshaller(formatSource = true)
    public static class ValidationLocalMethod extends Person {

        @Validator(validationMethod = "validateAge")
        private int age;

        public boolean validateAge(BiConsumer<String, Boolean> validatorLog){
            boolean valid = true;
            if(super.age > 40){
                valid = false;
                validatorLog.accept("too old, must be less than 40", false);
            }
            return valid;
        }

    }

    @CsvMarshaller(formatSource = true)
    public static class ValidationLMultipleoLcalMethod extends Person {

        @Validator(validationMethod = "validateAge")
        private int age;

        @Validator(validationMethod = "validateName")
        private String name;

        public boolean validateAge(BiConsumer<String, Boolean> validatorLog){
            boolean valid = true;
            if(super.age > 40){
                valid = false;
                validatorLog.accept("too old, must be less than 40", false);
            }
            return valid;
        }

        public boolean validateName(BiConsumer<String, Boolean> validatorLog){
            boolean valid = true;
            if(super.getName().startsWith("IGNORE")){
                valid = false;
                validatorLog.accept("IGNORE starts name", false);
            }
            return valid;
        }

    }

    @CsvMarshaller(formatSource = true)
    public static class NoNullWriteCheck extends Person {

        @DataMapping(checkNullOnWrite = false)
        private String name;
    }

    @CsvMarshaller(loopAssignmentLimit = 0)
    public static class LoopAssignment extends Person{

    }
}
