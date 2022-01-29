package com.fluxtion.extension.csvcompiler;

import org.junit.jupiter.api.Test;

public class SimpleMarshallerTest {

    @Test
    public void testCatMarshaller() throws ClassNotFoundException {
        String s = Cat.class.getCanonicalName() + "CsvMarshaller";
        Object o = Class.forName(Cat.class.getCanonicalName() + "CsvMarshaller");
        System.out.println(o);
    }
}
