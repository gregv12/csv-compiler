package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

import java.util.List;

@CsvMarshaller
@Data
public class ArrayBean {

    private int[] data;
    private String[] names;
    private List<Integer> scores;
    @ColumnMapping(optionalField = true)
    private List<String> venues;
}
