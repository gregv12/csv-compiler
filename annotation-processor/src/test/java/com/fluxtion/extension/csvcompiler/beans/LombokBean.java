package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

@Data
@CsvMarshaller(requireGetSetInSourceCode = false)
public class LombokBean {

    private String name;
    private int age;
}
