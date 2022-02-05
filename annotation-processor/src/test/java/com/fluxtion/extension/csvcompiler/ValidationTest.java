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
import org.junit.jupiter.api.Test;

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
                        validationResultStore.vaildationFailure("TOO OLD");
                },List.of(3),Person.build(Person::new, "tim", 32)
        );

    }
}
