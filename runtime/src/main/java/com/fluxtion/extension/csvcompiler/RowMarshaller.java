/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.ValidationLogger.ValidationResultStore;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.ServiceLoader.Provider;

public interface RowMarshaller<T> {

    /**
     * The target type of this RowMarshaller instance
     *
     * @return target type
     */
    Class<T> targetClass();

    /**
     * Validation logger
     *
     * @param errorLog
     * @return this {@link RowMarshaller} instance
     */
    RowMarshaller<T> setErrorLog(ValidationLogger errorLog);

    RowMarshaller<T> setValidator(BiConsumer<T, ValidationResultStore> validator);

    RowMarshaller<T> throwExceptionOnValidationFailure(boolean throwException);

    RowMarshaller<T> setHeaderTransformer(Function<String, String> headerTransformer);


    default RowMarshaller<T> addLookup(String lookupName, Function<CharSequence, CharSequence> lookup) {
        throw new IllegalArgumentException("cannot find lookup with name:" + lookup);
    }

    /**
     * Creates a stream from a supplied {@link Reader}
     *
     * @param in {@link Reader} to marshall from
     * @return Stream of target instances
     */
    Stream<T> stream(Reader in);

    /**
     * @param in
     * @return
     */
    default Stream<T> stream(String in) {
        return stream(new StringReader(in));
    }

    void forEach(Consumer<T> consumer, Reader in);

    Iterator<T> iterator(Reader in);

    static <T> RowMarshaller<T> load(Class<T> clazz) {
        return ServiceLoader.load(RowMarshaller.class).stream()
                .map(Provider::get)
                .map(obj -> (RowMarshaller<T>) obj)
                .filter(svc -> svc.targetClass().equals(clazz))
                .findAny()
                .get();
    }

}
