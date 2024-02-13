package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

public class TransformTest {

    @Data
    @Accessors(fluent = true)
    @CsvMarshaller(fluent = true)
    public static class BeanToChange{
        @ColumnMapping(columnName = "Current age")
        private int age;
        @ColumnMapping(outputField = false)
        private String Name;
    }

    @Test
    public void testTransForm(){
        StringReader reader = new StringReader("Current age,Name\n" +
                "12,Fred\n" +
                "13,Jean\n");
        StringWriter writer = new StringWriter();
        RowMarshaller.transform(BeanToChange.class, reader, writer, s -> s.peek(b -> b.age(b.age + 10)));
        Assertions.assertEquals("age\n22\n23",writer.toString().trim());
    }
}
