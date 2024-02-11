package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@CsvMarshaller(fluent = true)
@Accessors(fluent = true)
public class LombokFluentBean {

    private String name;
    private int age;
    @ColumnMapping(optionalField = true)
    private String adfs;
    @ColumnMapping(optionalField = true)
    private String MY_NAME;
}
