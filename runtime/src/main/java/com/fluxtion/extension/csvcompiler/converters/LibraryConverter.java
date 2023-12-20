package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;
import com.fluxtion.extension.csvcompiler.FieldConverter.NULL;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface LibraryConverter {
    static Class<? extends FieldConverter> getConverter(String name){
        ServiceLoader.load(FieldConverter.class).iterator();
        for (Iterator<FieldConverter> it = ServiceLoader.load(FieldConverter.class).iterator(); it.hasNext(); ) {
            FieldConverter<?> fieldConverter = it.next();
            if(fieldConverter.getId().equalsIgnoreCase(name)){
                return fieldConverter.getClass();
            }
        }
        return NULL.class;
    }
}
