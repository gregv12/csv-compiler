package com.fluxtion.extension.csvcompiler.beans;

public enum TestEnum {
    VALUE1, VALUE2;

    public static void main(String[] args) {
        System.out.println(TestEnum.valueOf("VALUE1"));
    }
}
