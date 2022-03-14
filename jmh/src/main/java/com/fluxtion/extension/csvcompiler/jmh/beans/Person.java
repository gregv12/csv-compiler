package com.fluxtion.extension.csvcompiler.jmh.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.ToString;

@ToString
@CsvMarshaller(formatSource = true, newBeanPerRecord = false)
public class Person {
    private CharSequence name;
    private double age;

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    @CsvMarshaller(formatSource = true, versionNumber = 1)
    public static class PersonBufferCopy extends Person{

    }

    @CsvMarshaller(formatSource = true, loopAssignmentLimit = 0, newBeanPerRecord = false)
    public static class PersonLoopAssignment extends Person{

    }
}
