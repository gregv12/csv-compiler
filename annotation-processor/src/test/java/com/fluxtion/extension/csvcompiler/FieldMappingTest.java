package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.Person;
import org.junit.jupiter.api.Test;

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
                        "tim  ,  32  \n" +
                        "  lisa  ,   44\n",
                Person.build(Person.NoTrimField::new, "tim  ", 32),
                Person.build(Person.NoTrimField::new, "  lisa  ", 44)
        );
    }


}
