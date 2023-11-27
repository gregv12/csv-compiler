package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.LombokBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class LombokTest {

    @Test
    public void streamCalculationSameInstanceTest() {
        String input = "name,age\n" +
                "tim,32\n" +
                "lisa,44\n";
        int sum = RowMarshaller.load(LombokBean.class).stream(new StringReader(input))
                .mapToInt(LombokBean::getAge)
                .sum();

        Assertions.assertEquals(76, sum);
    }
}