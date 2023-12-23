package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayByteConverter implements FieldConverter<byte[]> {

    public static final String ID = "converter.ToByteArray";
    @Override
    public byte[] fromCharSequence(CharSequence charSequence) {
        String[] tokens = charSequence.toString().split(",");
        byte[] result = new byte[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            result[i] =  Byte.parseByte(token);
        }
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
