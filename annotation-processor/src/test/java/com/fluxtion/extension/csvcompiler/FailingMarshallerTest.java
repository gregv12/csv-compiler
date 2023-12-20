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

import java.io.StringReader;

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
                Util.listOf(2, 4),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void noSkipCsvMarshallerEmptyLines() {
        testPersonErrors(
                Person.class,
                "name,age\n" +
                        "tim,32\n" +
                        "\n" +
                        "lisa,44\n",
                Util.listOf(3),
                Person.build(Person::new, "tim", 32),
                Person.build(Person::new, "lisa", 44)
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
                Util.listOf(2)
        ));
    }

//    @Test
//    public void missingLookupTest() {
//        String input = "name,age\n" +
//                "tim,32\n" +
//                "lisa,44\n";
//        Assertions.assertThrows(IllegalArgumentException.class, () ->
//                RowMarshaller.load(Person.Lookup.class)
//                        .addLookup("missing lookup key", s -> "40")
//                        .stream(new StringReader(input))
//                        .mapToInt(Person::getAge)
//                        .sum());
//    }

    @Test
    public void failFastWithExternalalidator(){

    }
}
