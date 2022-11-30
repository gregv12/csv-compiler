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

import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.processor.Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.fluxtion.extension.csvcompiler.SuccessfulMarshallerTest.testPersonErrors;

public class ValidationTest {

    @Test
    public void validationTest() {
        testPersonErrors(
                Person.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n",
                (person, validationResultStore) -> {
                    if (person.getAge() > 40)
                        validationResultStore.validationFailure("TOO OLD", false);
                },
                Util.listOf(3),
                Person.build(Person::new, "tim", 32)
        );
    }

    @Test
    public void interleavedInvalidRecordsValidationTest() {
        testPersonErrors(
                Person.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n" +
                        "siobhan,18\n"
                ,
                (person, validationResultStore) -> {
                    if (person.getAge() > 40)
                        validationResultStore.validationFailure("TOO OLD", false);
                }, Util.listOf(3),
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "siobhan", 18)
        );
    }

    @Test
    public void interleavedInvalidRecordsValidationFailFastTest() {
        List<Person> results = new ArrayList<>();
        RowMarshaller.load(Person.class)
                .setFatalExceptionHandler(c -> {
                })
                .setRowValidator((person, validationResultStore) -> {
                    if (person.getAge() > 40)
                        validationResultStore.validationFailure("TOO OLD", true);
                })
                .setValidationLogger(ValidationLogger.NULL)
                .stream("name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n" +
                        "siobhan,18\n")
                .forEach(results::add);

        Assertions.assertIterableEquals(Util.listOf(Person.build(Person::new, "tim", 32)), results);
    }

    @Test
    public void interleavedInvalidRecordsValidationFailFastWIthExceptionTest() {
        List<Person> results = new ArrayList<>();
        Assertions.assertThrows(CsvProcessingException.class, () -> RowMarshaller.load(Person.class)
                .setRowValidator((person, validationResultStore) -> {
                    if (person.getAge() > 40) {
                        validationResultStore.validationFailure("TOO OLD", true);
                    }
                })
                .setValidationLogger(ValidationLogger.NULL)
                .stream("name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n" +
                        "siobhan,18\n")
                .forEach(results::add)
        );
        Assertions.assertIterableEquals(Util.listOf(Person.build(Person::new, "tim", 32)), results);
    }

    @Test
    public void fieldValidation() {
        testPersonErrors(
                Person.Validation.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,-10\n" +
                        "siobhan,18\n"
                , Util.listOf(3),
                Person.build(Person.Validation::new, "tim", 32),
                Person.build(Person.Validation::new, "siobhan", 18)
        );
    }

    @Test
    public void fieldValidationWithLocalMethod() {
        testPersonErrors(
                Person.ValidationLocalMethod.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,410\n" +
                        "siobhan,18\n"
                , Util.listOf(3),
                Person.build(Person.ValidationLocalMethod::new, "tim", 32),
                Person.build(Person.ValidationLocalMethod::new, "siobhan", 18)
        );
    }

    @Test
    public void multipleFieldValidationWithLocalMethod() {
        testPersonErrors(
                Person.ValidationLMultipleoLcalMethod.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,410\n" +
                        "IGNORE tim,18\n" +
                        "siobhan,18\n"
                , Util.listOf(3,4),
                Person.build(Person.ValidationLMultipleoLcalMethod::new, "tim", 32),
                Person.build(Person.ValidationLMultipleoLcalMethod::new, "siobhan", 18)
        );
    }

    @Test
    public void failFastAnnotation() {
        List<Person.FailFast> results = new ArrayList<>();
        RowMarshaller.load(Person.FailFast.class)
                .setFatalExceptionHandler(c -> {
                })
                .setValidationLogger(ValidationLogger.NULL)
                .stream("name,age\n" +
                        "tim,32\n" +
                        "lisa,assdsdfefe\n" +
                        "siobhan,18\n")
                .forEach(results::add);

        Assertions.assertIterableEquals(Util.listOf(Person.build(Person.FailFast::new, "tim", 32)), results);
    }

    @Test
    public void failFastWithExceptionAnnotation() {
        List<Person.FailFast> results = new ArrayList<>();
        Assertions.assertThrows(CsvProcessingException.class, () -> RowMarshaller.load(Person.FailFast.class)
                .setValidationLogger(ValidationLogger.NULL)
                .stream("name,age\n" +
                        "tim,32\n" +
                        "lisa,assdsdfefe\n" +
                        "siobhan,18\n")
                .forEach(results::add)
        );

        Assertions.assertIterableEquals(Util.listOf(Person.build(Person.FailFast::new, "tim", 32)), results);
    }

    @Test
    public void noFailFastAnnotation() {
        List<Person> results = new ArrayList<>();
        RowMarshaller.load(Person.class)
                .setValidationLogger(ValidationLogger.NULL)
                .stream("name,age\n" +
                        "tim,32\n" +
                        "lisa,assdsdfefe\n" +
                        "siobhan,18\n")
                .forEach(results::add);

        Assertions.assertIterableEquals(Util.listOf(
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "siobhan", 18)
        ), results);
    }


}
