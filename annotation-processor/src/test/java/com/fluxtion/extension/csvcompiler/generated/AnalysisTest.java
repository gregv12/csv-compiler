package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import org.junit.jupiter.api.Test;

public class AnalysisTest {

    @Test
    public void adhocTest(){
        String data = "name,age,percentage\n" +
                "greg,,45.8\n" +
                "tim,90,22";
        RowMarshaller.load(Analysis.class)
                .stream(data)
                .forEach(System.out::println);
    }
}
