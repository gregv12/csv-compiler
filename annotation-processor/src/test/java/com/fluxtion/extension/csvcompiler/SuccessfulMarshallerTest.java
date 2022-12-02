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

package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.ValidationLogger.FailedRowValidationProcessor;
import com.fluxtion.extension.csvcompiler.beans.AllNativeMarshallerTypes;
import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.beans.Person.EscapedIndexFields;
import com.fluxtion.extension.csvcompiler.beans.Person.LookupDefaultValue;
import com.fluxtion.extension.csvcompiler.beans.Person.MultipleHeaderLines;
import com.fluxtion.extension.csvcompiler.processor.Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SuccessfulMarshallerTest {

    @Test
    public void loadPersonMarshallerTest() {
        RowMarshaller.load(Person.class);
    }

    @Test
    public void validateCsvToNewBeanPerRecordContentsTest() {
        testPerson(
                Person.class,
                "name,age\n" +
                "tim,32\n" +
                "lisa,44\n",
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void noLineEndingLastRowTest() {
        testPerson(
                Person.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,44",
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void oldMacLineEndingTest() {
        testPerson(
                Person.LegacyMacLineEnding.class,
                "name,age\r" +
                "tim,32\r" +
                "lisa,44\r",
                Person.build(Person.LegacyMacLineEnding::new, "tim", 32),
                Person.build(Person.LegacyMacLineEnding::new, "lisa", 44)
        );
    }

    @Test
    public void multipleHeaderLinesTest() {
        testPerson(
                MultipleHeaderLines.class,
                "XXXX\n" +
                        "name,age\n" +
                        "YYYYY\n" +
                        "tim,32\n" +
                        "lisa,44\n",
                Person.build(MultipleHeaderLines::new, "tim", 32),
                Person.build(MultipleHeaderLines::new, "lisa", 44)
        );
    }

    @Test
    public void validateCanSkipEmptyLines() {
        testPerson(
                Person.SkipEmptyLines.class,
                "name,age\n" +
                "tim,32\n" +
                "\n" +
                "lisa,44\n",
                Person.build(Person.SkipEmptyLines::new, "tim", 32),
                Person.build(Person.SkipEmptyLines::new, "lisa", 44)
        );
    }

    @Test
    public void validateCanSkipCommentLines() {
        testPerson(
                Person.class,
                "name,age\n" +
                        "#\n" +
                        "#\n" +
                        "#\n" +
                        "tim,32\n" +
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
    public void processEscapeSequence(){
        testPerson(
                Person.Escaped.class,
                "name,age\n" +
                        "\"t, s\",32\n" +
                        "\"\"\"lisa\"\"\",44\n"
                ,
                Person.build(Person.Escaped::new, "t, s", 32),
                Person.build(Person.Escaped::new, "\"lisa\"", 44)
        );
    }


    @Test
    public void processIndexEscapeSequence(){
        testPerson(
                EscapedIndexFields.class,
                        "\"\"\"L\"\"\",44\n"
                ,
                Person.build(EscapedIndexFields::new, "\"L\"", 44)
        );
    }

    @Test
    public void processPipeAsSeparatorSequence(){
        testPerson(
                Person.PipeSeparator.class,
                "name|age\n" +
                        "tim|32\n" +
                        "lisa|44\n"
                ,
                Person.build(Person.PipeSeparator::new, "tim", 32),
                Person.build(Person.PipeSeparator::new, "lisa", 44)
        );
    }

    @Test
    public void acceptPartialsTest(){
        testPerson(
                Person.AcceptPartials.class,
                "name,age\n" +
                        "tim\n" +
                        "lisa,44\n"
                ,
                Person.build(Person.AcceptPartials::new, "tim", 0),
                Person.build(Person.AcceptPartials::new, "lisa", 44)
        );
    }

    @Test
    public void trimTest(){
        testPerson(
                Person.Trim.class,
                "name,age\n" +
                        "tim ,  32 \n" +
                        "  lisa, 44\n"
                ,
                Person.build(Person.Trim::new, "tim", 32),
                Person.build(Person.Trim::new, "lisa", 44)
        );
    }

    @Test
    public void postProcessMethodTest() {
        testPerson(
                Person.PostProcess.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n",
                Person.build(Person.PostProcess::new, "TIM", 32),
                Person.build(Person.PostProcess::new, "LISA", 44)
        );
    }

    @Test
    public void lookupFieldTest() {
        String input = "name,age\n" +
                "tim,32\n" +
                "lisa,44\n";
        int sum = RowMarshaller.load(Person.Lookup.class)
                .addLookup(Person.Lookup.AGE_LOOKUP, s -> "40")
                .stream(new StringReader(input))
                .mapToInt(Person::getAge)
                .sum();
        Assertions.assertEquals(80, sum);
    }

    @Test
    public void lookupDefaultFieldTest(){
        //LookupDefaultValue
        String input = "name,age\n" +
                ",32\n" +
                "lisa,44\n";
        int count = RowMarshaller.load(Person.LookupDefaultValue.class)
                .addLookup(LookupDefaultValue.NAME_LOOKUP, s -> {
                    if(s.toString().equalsIgnoreCase("tom")){
                        return "FOUND";
                    }
                    return s;
                })
                .stream(new StringReader(input))
                .map(Person::getName)
                .filter("FOUND"::equals)
                .mapToInt(s -> 1)
                .sum()
        ;
        Assertions.assertEquals(1, count);
    }

    @Test
    public void allDefaultPropertyTypeMarshaller() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                       "true,8,10.7,1.5,100,2000,4,hello\n";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class).forEach(resultList::add, new StringReader(input));

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
                Util.listOf(bean),
                resultList
        );
    }

    @Test
    public void mapHeaderTest(){
        String input = "booleanPROP,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "true,8,10.7,1.5,100,2000,4,hello\n";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class)
                .setHeaderTransformer(s -> s.replace("PROP", "Property"))
                .forEach(resultList::add, new StringReader(input));

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
                Util.listOf(bean),
                resultList
        );
    }

    public static void parserOutput(RowMarshaller<?> loader, String input){
        loader.forEach(System.out::println, new StringReader(input));
    }

    @SafeVarargs
    static <T extends Person> void testPerson(Class<T> personClass, String input, T... people) {
        testPersonErrors(personClass, input, Util.listOf(), people);
    }

    @SafeVarargs
    static <T extends Person> void testPersonErrors(
            Class<T> personClass, String input, List<Integer> errorRowsExpected, T... people) {
        testPersonErrors(personClass, input, null, errorRowsExpected, people);
    }

    @SafeVarargs
    static <T extends Person> void testPersonErrors(
            Class<T> personClass, String input, BiConsumer<T, FailedRowValidationProcessor> validator, List<Integer> errorRowsExpected, T... people) {
        List<? super Person> resultList = new ArrayList<>();
        List<Integer> errorRowsActual = new ArrayList<>();

        RowMarshaller
                .load(personClass)
                .setValidationLogger(new ValidationLogger() {
                    @Override
                    public void logFatal(CsvProcessingException csvProcessingException) {
//                        System.out.println(csvProcessingException);
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }

                    @Override
                    public void logWarning(CsvProcessingException csvProcessingException) {
//                        System.out.println(csvProcessingException);
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }
                })
                .setRowValidator(validator)
                .forEach(resultList::add, new StringReader(input));

        assertIterableEquals(
                errorRowsExpected,
                errorRowsActual,
                "failed rows do not match"
        );

        assertIterableEquals(
                Util.listOf(people),
                resultList,
                "number of valid rows is different"
        );

        //perform test with streaming
        errorRowsActual.clear();
        resultList = RowMarshaller
                .load(personClass)
                .setValidationLogger(new ValidationLogger() {
                    @Override
                    public void logFatal(CsvProcessingException csvProcessingException) {
//                        System.out.println(csvProcessingException);
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }

                    @Override
                    public void logWarning(CsvProcessingException csvProcessingException) {
//                        System.out.println(csvProcessingException);
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }
                })
                .setRowValidator(validator)
                .stream(input).collect(Collectors.toList());

        assertIterableEquals(
                errorRowsExpected,
                errorRowsActual,
                "streaming indexes of invalid rows does not match"
        );

        assertIterableEquals(
                Util.listOf(people),
                resultList,
                "streaming number of valid rows is different"
        );
    }
}
