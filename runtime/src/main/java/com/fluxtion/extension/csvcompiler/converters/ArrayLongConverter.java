package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayLongConverter implements FieldConverter<long[]> {

    public static final String ID = "converter.ToLongArray";
    @Override
    public long[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        long[] result = new long[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Long.parseLong(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
