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
import com.fluxtion.extension.csvcompiler.beans.Person.ConverterField;
import com.fluxtion.extension.csvcompiler.beans.Person.ConverterFieldLocalMethod;
import com.fluxtion.extension.csvcompiler.beans.Person.ConverterFieldLoopAssignment;
import com.fluxtion.extension.csvcompiler.beans.Person.DerivedFieldLocalMethod;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static com.fluxtion.extension.csvcompiler.SuccessfulMarshallerTest.testPerson;

public class FieldMappingTest {


    @Test
    public void mapColumnTest() {
        testPerson(
                Person.MapColumn.class,
                "overrideNameMapping,age\n" +
                        "tim,32\n" +
                        "lisa,44\n",
                Person.build(Person.MapColumn::new, "tim", 32),
                Person.build(Person.MapColumn::new, "lisa", 44)
        );
    }


    @Test
    public void defaultColumnValueTest() {
        testPerson(
                Person.DefaultColumnValue.class,
                "name,age\n" +
                        ",32\n" +
                        "lisa,\n",
                Person.build(Person.DefaultColumnValue::new, "NO NAME", 32),
                Person.build(Person.DefaultColumnValue::new, "lisa", 18)
        );
    }

    @Test
    public void trimOneField() {
        testPerson(
                Person.NoTrimField.class,
                "name,age\n" +
                        "tim  ,32\n" +
                        "  lisa  ,44\n",
                Person.build(Person.NoTrimField::new, "tim", 32),
                Person.build(Person.NoTrimField::new, "lisa", 44)
        );
    }


    @Test
    public void optionalFieldColumnMissing() {
        testPerson(
                Person.OptionalField.class,
                "name\n" +
                        "tim\n" +
                        "lisa\n",
                Person.build(Person.OptionalField::new, "tim", 0),
                Person.build(Person.OptionalField::new, "lisa", 0)
        );
    }

    @Test
    public void optionalFieldColumnPresent() {
        testPerson(
                Person.OptionalField.class,
                "name,age\n" +
                        "tim,32\n" +
                        "lisa,44\n",
                Person.build(Person.OptionalField::new, "tim", 32),
                Person.build(Person.OptionalField::new, "lisa", 44)
        );
    }

    @Test
    public void optionalFieldColumnMissingDefaultValue() {
        testPerson(
                Person.OptionalFieldWithDefaultValue.class,
                "name\n" +
                        "tim\n" +
                        "lisa\n",
                Person.build(Person.OptionalFieldWithDefaultValue::new, "tim", 18),
                Person.build(Person.OptionalFieldWithDefaultValue::new, "lisa", 18)
        );
    }

    @Test
    public void indexColumnsNoHeader() {
        testPerson(
                Person.IndexFields.class,
                        "tim,32\n" +
                        "lisa,44\n",
                Person.build(Person.IndexFields::new, "tim", 32),
                Person.build(Person.IndexFields::new, "lisa", 44)
        );
    }

    @Test
    public void converterFields(){
        ConverterField converted = new ConverterField();
        converted.setAge(44);
        converted.setName("ALWAYS_LINDA");
        converted.setBirthTime(LocalTime.of(12,34));
        testPerson(
                ConverterField.class,
                "name,age,birthTime\n" +
                "lisa,44,12:34\n",
                converted
        );

    }

    @Test
    public void converterFieldsWithLocalMappingMethod(){
        ConverterFieldLocalMethod converted = new ConverterFieldLocalMethod();
        converted.setAge(44);
        converted.setName("ALWAYS_LINDA");
        converted.setBirthTime(LocalTime.of(12,34));
        testPerson(
                ConverterFieldLocalMethod.class,
                "name,age,birthTime\n" +
                        "lisa,44,12:34\n",
                converted
        );

    }

    @Test
    public void derivedFieldsWithLocalMappingMethodIsAfterRealColumns(){
        DerivedFieldLocalMethod converted = new DerivedFieldLocalMethod();
        converted.setAge(44);
        converted.setName("lisa");
        converted.setBirthTime(LocalTime.of(12,34));
        converted.setDerivedName(true);
        testPerson(
                DerivedFieldLocalMethod.class,
                "name,age,birthTime\n" +
                        "lisa,44,12:34\n",
                converted
        );

    }

    @Test
    public void converterFieldsLoopAssignment(){
        ConverterFieldLoopAssignment converted = new ConverterFieldLoopAssignment();
        converted.setAge(44);
        converted.setName("ALWAYS_LINDA");
        converted.setBirthTime(LocalTime.of(12,34));
        testPerson(
                ConverterFieldLoopAssignment.class,
                "name,age,birthTime\n" +
                "lisa,44,12:34\n",
                converted
        );

    }

}
