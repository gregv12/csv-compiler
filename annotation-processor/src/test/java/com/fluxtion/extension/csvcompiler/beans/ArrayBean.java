package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import lombok.Data;

@CsvMarshaller
@Data
public class ArrayBean {

    private int[] data;
    private String[] names;
}
