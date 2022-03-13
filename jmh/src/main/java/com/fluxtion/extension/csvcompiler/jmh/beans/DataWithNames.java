package com.fluxtion.extension.csvcompiler.jmh.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.jmh.beans.Person;

@CsvMarshaller(formatSource = true, newBeanPerRecord = false)
public class DataWithNames extends Person {

    private CharSequence name1;
    private CharSequence name2;
    private CharSequence name3;
    private CharSequence name4;
    private CharSequence name5;
    private CharSequence name6;
    private CharSequence name7;
    private CharSequence name8;
    private CharSequence name9;


    public CharSequence getName1() {
        return name1;
    }

    public void setName1(CharSequence name1) {
        this.name1 = name1;
    }

    public CharSequence getName2() {
        return name2;
    }

    public void setName2(CharSequence name2) {
        this.name2 = name2;
    }

    public CharSequence getName3() {
        return name3;
    }

    public void setName3(CharSequence name3) {
        this.name3 = name3;
    }

    public CharSequence getName4() {
        return name4;
    }

    public void setName4(CharSequence name4) {
        this.name4 = name4;
    }

    public CharSequence getName5() {
        return name5;
    }

    public void setName5(CharSequence name5) {
        this.name5 = name5;
    }

    public CharSequence getName6() {
        return name6;
    }

    public void setName6(CharSequence name6) {
        this.name6 = name6;
    }

    public CharSequence getName7() {
        return name7;
    }

    public void setName7(CharSequence name7) {
        this.name7 = name7;
    }

    public CharSequence getName8() {
        return name8;
    }

    public void setName8(CharSequence name8) {
        this.name8 = name8;
    }

    public CharSequence getName9() {
        return name9;
    }

    public void setName9(CharSequence name9) {
        this.name9 = name9;
    }

    @CsvMarshaller(formatSource = true, versionNumber = 1)
    public static class DataWithNamesNoBuffer extends DataWithNames{}

}
