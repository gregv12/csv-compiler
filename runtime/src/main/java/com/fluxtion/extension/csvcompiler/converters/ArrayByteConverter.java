package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayByteConverter implements FieldConverter<byte[]> {

    public static final String ID = "converter.ToByteArray";
    public static final byte[] EMPTY_ARRAY = new byte[0];
    @Override
    public byte[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        String[] tokens = charSequence.toString().split("\\|");
        byte[] result = new byte[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Byte.parseByte(token);
        }
        return result;
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(byte[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            byte i = field[j];
            target.append(Byte.toString(i));
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
