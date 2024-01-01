package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayFloatConverter implements FieldConverter<float[]> {

    public static final String ID = "converter.ToFloatArray";
    public static final float[] EMPTY_ARRAY = new float[0];
    @Override
    public float[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        float[] result = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Float.parseFloat(token);
        }
        return result;
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(float[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            float i = field[j];
            target.append(Float.toString(i));
            if (j != field.length - 1) {
                target.append(Conversion.ARRAY_DELIMITER);
            }
        }
        target.append("\"");
    }

    @Override
    public String getId() {
        return ID;
    }
}
