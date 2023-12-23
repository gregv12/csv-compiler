package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayShortConverter implements FieldConverter<short[]> {

    public static final String ID = "converter.ToShortArray";
    @Override
    public short[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        short[] result = new short[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Short.parseShort(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
