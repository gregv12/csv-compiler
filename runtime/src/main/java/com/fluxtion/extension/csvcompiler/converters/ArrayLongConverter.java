package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayLongConverter implements FieldConverter<long[]> {

    public static final String ID = "converter.ToLongArray";
    public static final long[] EMPTY_ARRAY = new long[0];
    @Override
    public long[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        long[] result = new long[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Long.parseLong(token);
        }
        return result;
    }


    @lombok.SneakyThrows
    @Override
    public void toCharSequence(long[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            long i = field[j];
            target.append(Long.toString(i));
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
