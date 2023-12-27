package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.google.auto.service.AutoService;

@AutoService(FieldConverter.class)
public class ArrayStringConverter implements FieldConverter<String[]> {

    public static final String ID = "converter.ToStringArray";
    public static final String[] EMPTY_ARRAY = new String[0];
    @Override
    public String[] fromCharSequence(CharSequence charSequence) {
        if(charSequence.length() == 0){
            return EMPTY_ARRAY;
        }
        return charSequence.toString().split("\\|");
    }

    @lombok.SneakyThrows
    @Override
    public void toCharSequence(String[] field, Appendable target) {
        target.append("\"");
        for (int j = 0; j < field.length; j++) {
            target.append(field[j]);
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
