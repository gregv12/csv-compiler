package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

@CsvMarshaller(mappingRow = 1, processEscapeSequences = true, formatSource = true, skipCommentLines = true)
public class Cat {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
