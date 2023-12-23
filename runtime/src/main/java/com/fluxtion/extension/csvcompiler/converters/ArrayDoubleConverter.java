package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayDoubleConverter implements FieldConverter<double[]> {

    public static final String ID = "converter.ToDoubleArray";
    @Override
    public double[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        double[] result = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Double.parseDouble(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
