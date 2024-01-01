package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListIntegerConverter implements FieldConverter<List<Integer>> {

    public static final String ID = "converter.ToIntList";

    private final ArrayIntConverter arrayIntConverter = new ArrayIntConverter();

    @Override
    public List<Integer> fromCharSequence(CharSequence charSequence) {
        List<Integer> list = new ArrayList<>();
        for (int anInt : arrayIntConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Integer> field, Appendable target) {
        arrayIntConverter.toCharSequence(field.stream().mapToInt(Integer::intValue).toArray(), target);
    }

    @Override
    public String getId() {
        return ID;
    }
}
