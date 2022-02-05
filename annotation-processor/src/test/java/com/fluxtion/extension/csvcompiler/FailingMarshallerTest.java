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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fluxtion.extension.csvcompiler.SuccessfulMarshallerTest.testPersonErrors;

public class FailingMarshallerTest {

    @Test
    public void recordMultipleErrors() {
        testPersonErrors(
                Person.class,
                "name,age\n" +
                        "tim,sfgdg\n" +
                        "lisa,44\n" +
                        "lisa,fddg\n",
                List.of(2,4),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void noSkipCsvMarshallerEmptyLines() {
        testPersonErrors(
                Person.NoSkip.class,
                "name,age\n" +
                        "tim,32\n" +
                        "\n" +
                        "lisa,44\n",
                List.of(3),
                Person.build(Person.NoSkip::new, "tim", 32),
                Person.build(Person.NoSkip::new, "lisa", 44)
        );
    }

    @Test
    public void failFastTest() {
        Assertions.assertThrows(CsvProcessingException.class, () -> testPersonErrors(
                Person.FailFast.class,
                "name,age\n" +
                        "tim,dfrfrf\n" +
                        "\n" +
                        "lisa\n",
                List.of(2)
        ));
    }
}
