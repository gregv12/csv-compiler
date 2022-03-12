package com.fluxtion.extension.csvcompiler.jmh.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.ToString;

@ToString
@CsvMarshaller(formatSource = true, newBeanPerRecord = false)
public class NameOnly {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
