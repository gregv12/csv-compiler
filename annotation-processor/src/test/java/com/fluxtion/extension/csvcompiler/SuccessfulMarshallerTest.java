/*
 *
 *  * Copyright 2022-2022 greg higgins
 *  *
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.AllNativeMarshallerTypes;
import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.beans.Person.MultipleHeaderLines;
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
    public void unixLineEndingTest() {
        testPerson(
                Person.UnixLineEnding.class,
                "name,age\r" +
                "tim,32\r" +
                "\r" +
                "lisa,44\r",
                Person.build(Person.UnixLineEnding::new, "tim", 32),
                Person.build(Person.UnixLineEnding::new, "lisa", 44)
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
                        "\n" +
                        "lisa,44\n",
                Person.build(MultipleHeaderLines::new, "tim", 32),
                Person.build(MultipleHeaderLines::new, "lisa", 44)
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
    public void validateCanSkipCommentLines() {
        testPerson(
                Person.class,
                "name,age\n" +
                        "#\n" +
                        "#\n" +
                        "#\n" +
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
    public void processEscapeSequence(){
        testPerson(
                Person.Escaped.class,
                "name,age\n" +
                        "\"tim, smith\",32\n" +
                        "\n" +
                        "\"\"\"lisa\"\"\",44\n"
                ,
                Person.build(Person.Escaped::new, "tim, smith", 32),
                Person.build(Person.Escaped::new, "\"lisa\"", 44)
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

    public static void parserOutput(CsvMarshallerLoader loader, String input){
        loader.stream(System.out::println, new StringReader(input));
    }

    @SafeVarargs
    static <T extends Person> void testPerson(Class<T> personClass, String input, T... people) {
        testPersonErrors(personClass, input, List.of(), people);
    }

    @SafeVarargs
    static <T extends Person> void testPersonErrors(
            Class<T> personClass, String input, List<Integer> errorRowsExpected, T... people) {
        List<? super Person> resultList = new ArrayList<>();
        List<Integer> errorRowsActual = new ArrayList<>();

        CsvMarshallerLoader
                .marshaller(personClass)
                .setErrorLog(new ValidationLogger() {
                    @Override
                    public void logFatal(CsvProcessingException csvProcessingException) {
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }

                    @Override
                    public void logException(CsvProcessingException csvProcessingException) {
                        errorRowsActual.add(csvProcessingException.getLineNumber());
                    }
                })
                .stream(resultList::add, new StringReader(input));

        assertIterableEquals(
                List.of(people),
                resultList
        );

        assertIterableEquals(
                errorRowsExpected,
                errorRowsActual
        );
    }
}
