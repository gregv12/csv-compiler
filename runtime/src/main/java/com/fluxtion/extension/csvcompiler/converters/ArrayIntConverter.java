package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayIntConverter implements FieldConverter<int[]> {

    public static final String ID = "converter.ToIntArray";
    public static final int[] EMPTY_ARRAY = new int[0];


    @Override
    public int[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        int[] result = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] = Integer.parseInt(token);
        }
        return result;
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(int[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            int i = field[j];
            target.append(Integer.toString(i));
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
