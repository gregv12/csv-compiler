package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayDoubleConverter implements FieldConverter<double[]> {

    public static final String ID = "converter.ToDoubleArray";
    public static final double[] EMPTY_ARRAY = new double[0];
    @Override
    public double[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        double[] result = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Double.parseDouble(token);
        }
        return result;
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(double[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            double i = field[j];
            target.append(Double.toString(i));
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
