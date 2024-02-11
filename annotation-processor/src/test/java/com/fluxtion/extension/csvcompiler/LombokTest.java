package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.LombokBean;
import com.fluxtion.extension.csvcompiler.beans.LombokFluentBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;

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


    @Test
    public void streamCalculationSameInstanceFluentTest() {
        String input = "name,age,MY_NAME\n" +
                "tim,32,TIM\n" +
                "lisa,44,LISA\n";
        int sum = RowMarshaller.load(LombokFluentBean.class).stream(new StringReader(input))
                .mapToInt(LombokFluentBean::age)
                .sum();

        Assertions.assertEquals(76, sum);

        Set<String> NY_NAME_set = RowMarshaller.load(LombokFluentBean.class).stream(new StringReader(input))
                .map(LombokFluentBean::MY_NAME)
                .collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("TIM", "LISA"), NY_NAME_set);
    }
}
