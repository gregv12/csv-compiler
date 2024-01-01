package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListShortConverter implements FieldConverter<List<Short>> {

    public static final String ID = "converter.ToShortList";

    private final ArrayShortConverter arrayIntConverter = new ArrayShortConverter();

    @Override
    public List<Short> fromCharSequence(CharSequence charSequence) {
        List<Short> list = new ArrayList<>();
        for (short anInt : arrayIntConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Short> field, Appendable target) {
        short[] byteArray = new short[field.size()];
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
