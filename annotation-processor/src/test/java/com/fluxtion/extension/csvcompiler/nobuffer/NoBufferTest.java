package com.fluxtion.extension.csvcompiler.nobuffer;

import com.fluxtion.extension.csvcompiler.processor.nobuffer.Person;
import com.fluxtion.extension.csvcompiler.processor.nobuffer.PersonCsvMarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class NoBufferTest {

    @Test
    public void streamCalculationNewInstanceTest() {
        PersonCsvMarshaller marshaller = new PersonCsvMarshaller();
        String input = "name,age\n" +
                "tim,32\n" +
                "lisa,44\n";
        int sum = marshaller.stream(new StringReader(input))
                .mapToInt(Person::getAge)
                .sum();
        Assertions.assertEquals(76, sum);
    }
}
