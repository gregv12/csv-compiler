package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayShortConverter implements FieldConverter<short[]> {

    public static final String ID = "converter.ToShortArray";
    public static final short[] EMPTY_ARRAY = new short[0];
    @Override
    public short[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        short[] result = new short[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Short.parseShort(token);
        }
        return result;
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(short[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            short i = field[j];
            target.append(Short.toString(i));
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
