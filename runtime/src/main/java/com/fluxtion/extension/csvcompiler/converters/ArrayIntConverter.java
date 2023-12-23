package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayIntConverter implements FieldConverter<int[]> {

    public static final String ID = "converter.ToIntArray";
    @Override
    public int[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        int[] result = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Integer.parseInt(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
