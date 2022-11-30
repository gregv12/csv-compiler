package com.fluxtion.extension;

public interface FieldAccessor {
    <T> T getField(String fieldName);
}
