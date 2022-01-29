package com.fluxtion.extension.csvcompiler;

import java.io.Reader;
import java.util.function.Consumer;

import static java.util.ServiceLoader.Provider;
import static java.util.ServiceLoader.load;

public interface CsvMarshallerLoader<T> {

    Class<T> targetClass();

    static<T> CsvMarshallerLoader<T> marshaller(Class<T> clazz){
        return load(CsvMarshallerLoader.class).stream()
                .map(Provider::get)
                .map(obj -> (CsvMarshallerLoader<T>)obj)
                .filter(svc -> svc.targetClass().equals(clazz))
                .findAny()
                .get();
    }

    CsvMarshallerLoader<T> setErrorLog(ValidationLogger errorLog);

    void stream(Consumer<T> consumer, Reader in);

}
