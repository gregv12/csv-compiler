package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;
import com.fluxtion.extension.csvcompiler.converters.LocalTimeConverter;
import com.fluxtion.extension.csvcompiler.converters.ConstantStringConverter;
import lombok.ToString;

import java.time.LocalTime;

@ToString
@CsvMarshaller(formatSource = true)
public class Person {

    @DataMapping(converter = ConstantStringConverter.class, configuration = "ALWAYS ME")
    private String name;
    private int age;

    @DataMapping(converter = LocalTimeConverter.class)
    private LocalTime birthDate;

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

    public LocalTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalTime birthDate) {
        this.birthDate = birthDate;
    }
}
