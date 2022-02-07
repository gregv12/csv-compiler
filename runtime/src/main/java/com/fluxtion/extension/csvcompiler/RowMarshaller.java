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
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.ServiceLoader.Provider;

public interface RowMarshaller<T> {

    Class<T> targetClass();

    RowMarshaller<T> setErrorLog(ValidationLogger errorLog);

    void stream(Consumer<T> consumer, Reader in);

    RowMarshaller<T> setValidator(BiConsumer<T, ValidationResultStore> validator);

    static <T> RowMarshaller<T> load(Class<T> clazz) {
        return ServiceLoader.load(RowMarshaller.class).stream()
                .map(Provider::get)
                .map(obj -> (RowMarshaller<T>) obj)
                .filter(svc -> svc.targetClass().equals(clazz))
                .findAny()
                .get();
    }

}
