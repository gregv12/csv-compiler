package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListStringConverter implements FieldConverter<List<String>> {

    public static final String ID = "converter.ToStringList";

    private final ArrayStringConverter arrayIntConverter = new ArrayStringConverter();

    @Override
    public List<String> fromCharSequence(CharSequence charSequence) {
        return new ArrayList<>(Arrays.asList(arrayIntConverter.fromCharSequence(charSequence)));
    }

    @Override
    public void toCharSequence(List<String> field, Appendable target) {
        if(field != null){
            arrayIntConverter.toCharSequence(field.toArray(new String[0]), target);
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
