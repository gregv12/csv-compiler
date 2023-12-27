package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListFloatConverter implements FieldConverter<List<Float>> {

    public static final String ID = "converter.ToFloatList";

    private final ArrayFloatConverter arrayIntConverter = new ArrayFloatConverter();

    @Override
    public List<Float> fromCharSequence(CharSequence charSequence) {
        List<Float> list = new ArrayList<>();
        for (float anInt : arrayIntConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Float> field, Appendable target) {
        float[] byteArray = new float[field.size()];
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
