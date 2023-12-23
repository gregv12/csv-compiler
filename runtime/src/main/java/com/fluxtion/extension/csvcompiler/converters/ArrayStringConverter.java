package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayStringConverter implements FieldConverter<String[]> {

    public static final String ID = "converter.ToStringArray";
    @Override
    public String[] fromCharSequence(CharSequence charSequence) {
        return charSequence.toString().split(",");
    }

    @Override
    public String getId() {
        return ID;
    }
}
