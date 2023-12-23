package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayFloatConverter implements FieldConverter<float[]> {

    public static final String ID = "converter.ToFloatArray";
    @Override
    public float[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        float[] result = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Float.parseFloat(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
