package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListLongConverter implements FieldConverter<List<Long>> {

    public static final String ID = "converter.ToLongList";

    private final ArrayLongConverter arrayLongConverter = new ArrayLongConverter();

    @Override
    public List<Long> fromCharSequence(CharSequence charSequence) {
        List<Long> list = new ArrayList<>();
        for (long anInt : arrayLongConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Long> field, Appendable target) {
        arrayLongConverter.toCharSequence(field.stream().mapToLong(Long::longValue).toArray(), target);
    }

    @Override
    public String getId() {
        return ID;
    }
}
