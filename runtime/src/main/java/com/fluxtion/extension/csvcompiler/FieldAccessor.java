package com.fluxtion.extension.csvcompiler;

public interface FieldAccessor {
    <T> T getField(String fieldName);
}
