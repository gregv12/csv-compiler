package com.fluxtion.extension.csvcompiler.beans;

public interface FieldAccessor {
    <T> T getField(String fieldName);
}
