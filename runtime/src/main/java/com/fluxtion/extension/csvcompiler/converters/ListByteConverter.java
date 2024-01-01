package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListByteConverter implements FieldConverter<List<Byte>> {

    public static final String ID = "converter.ToByteList";

    private final ArrayByteConverter arrayIntConverter = new ArrayByteConverter();

    @Override
    public List<Byte> fromCharSequence(CharSequence charSequence) {
        List<Byte> list = new ArrayList<>();
        for (byte anInt : arrayIntConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Byte> field, Appendable target) {
        byte[] byteArray = new byte[field.size()];
        for (int i = 0; i < field.size(); i++) {
            byteArray[i] = field.get(i);
        }
        arrayIntConverter.toCharSequence(byteArray, target);
    }

    @Override
    public String getId() {
        return ID;
    }
}
