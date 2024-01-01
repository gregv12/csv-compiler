package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.List;

public class ListDoubleConverter implements FieldConverter<List<Double>> {

    public static final String ID = "converter.ToDoubleList";

    private final ArrayDoubleConverter arrayIntConverter = new ArrayDoubleConverter();

    @Override
    public List<Double> fromCharSequence(CharSequence charSequence) {
        List<Double> list = new ArrayList<>();
        for (double anInt : arrayIntConverter.fromCharSequence(charSequence)) {
            list.add(anInt);
        }
        return list;
    }

    @Override
    public void toCharSequence(List<Double> field, Appendable target) {
        arrayIntConverter.toCharSequence(field.stream().mapToDouble(Double::doubleValue).toArray(), target);
    }

    @Override
    public String getId() {
        return ID;
    }
}
