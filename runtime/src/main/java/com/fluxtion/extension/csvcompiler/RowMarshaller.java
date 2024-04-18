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

import com.fluxtion.extension.csvcompiler.ValidationLogger.FailedRowValidationProcessor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A class that provides a marshalling service that converts row style character data into a stream of marshalled instances.
 *
 * @param <T>
 */
public interface RowMarshaller<T> {

    /**
     * The target type of this RowMarshaller instance
     *
     * @return target type
     */
    Class<T> targetClass();

    /**
     * Set {@link ValidationLogger} for this RowMarshaller
     *
     * @param errorLog the error log service
     * @return this {@link RowMarshaller} instance
     */
    RowMarshaller<T> setValidationLogger(ValidationLogger errorLog);

    /**
     * Set the row validation logic for this RowMarshaller. Receives a callback for each successfully marshalled instance
     * and {@link FailedRowValidationProcessor} to process any validation failures.
     *
     * @param validator The injected row validation logic
     * @return this {@link RowMarshaller} instance
     */
    RowMarshaller<T> setRowValidator(BiConsumer<T, FailedRowValidationProcessor> validator);

    /**
     * Inject a fatal exception handler into the processing loop. If a fatal exception occurs this instance receives the
     * exception for processing. A default instance is provided that rethrows the {@link CsvProcessingException}
     *
     * @param fatalExceptionHandler application provided exception handler
     * @return this {@link RowMarshaller} instance
     */
    RowMarshaller<T> setFatalExceptionHandler(Consumer<CsvProcessingException> fatalExceptionHandler);

    /**
     * Callback to inject application logic for transforming the raw header before processing by the RowMarshaller
     *
     * @param headerTransformer header transformer
     * @return this {@link RowMarshaller} instance
     */
    RowMarshaller<T> setHeaderTransformer(Function<String, String> headerTransformer);

    /**
     * Any field annotated with {@link com.fluxtion.extension.csvcompiler.annotations.DataMapping} lookupName set to
     * a non blank value will the lookup function registered under a matching name to convert the supplied {@link CharSequence}
     *
     * @param lookupName The lookup name
     * @param lookup     The lookup {@link Function}
     * @return this {@link RowMarshaller} instance
     */
    default RowMarshaller<T> addLookup(String lookupName, Function<CharSequence, CharSequence> lookup) {
        return this;
    }

    /**
     * Creates a {@link Stream} from a supplied {@link Reader}
     *
     * @param in {@link Reader} to marshall from
     * @return Stream of target instances
     */
    Stream<T> stream(Reader in);

    /**
     * Creates a {@link Stream}  from a supplied String
     *
     * @param in String to marshall from
     * @return Stream of target instances
     */
    default Stream<T> stream(String in) {
        return stream(new StringReader(in));
    }

    /**
     * Apply for each semantics over generated instance from the supplied {@link Reader}
     *
     * @param consumer Marshalled instance consumer
     * @param in       Reader source to process
     */
    void forEach(Consumer<? super T> consumer, Reader in);

    /**
     * Creates an iterator over marshalled instances from the supplied {@link Reader}
     *
     * @param in Reader source to process
     * @return The iterator over marshalled instances
     */
    Iterator<T> iterator(Reader in);

    /**
     * Loads a {@link RowMarshaller} for a class type using the {@link ServiceLoader} paradigm
     *
     * @param clazz The class of the Marshalled instance
     * @param <T>   The RowMarshaller output type
     * @return A Configured {@link RowMarshaller} instance for the output type
     */
    @SuppressWarnings("unchecked")
    static <T> RowMarshaller<T> load(Class<T> clazz) {
        for (RowMarshaller<T> rowMarshaller : ServiceLoader.load(RowMarshaller.class)) {
            if (rowMarshaller.targetClass().equals(clazz)) {
                return rowMarshaller;
            }
        }
        throw new RuntimeException("unable to find RowMarshaller registered with ServiceLoader, class:" + clazz);
    }

    /**
     * Creates a SingleRowMarshaller for this {@link RowMarshaller}
     * @return SingleRowMarshaller
     */
    default SingleRowMarshaller<T> parser(){
        return new SingleRowMarshaller<>(this);
    }

    /**
     * Transforms a masrshalled bean using an injected {@link Stream} operation. The input is read from reader path
     * and written to the writer path, The internal reader and writer are close when this operation terminates.
     *
     * @param readerPath The path to read input data from
     * @param writerPath The path to write the transformed data to
     * @param transformer user supplied stream operation
     */
    @SneakyThrows
    default void transform(Path readerPath, Path writerPath, UnaryOperator<Stream<T>> transformer) {
        try (Writer writer = Files.newBufferedWriter(writerPath)) {
            try (Reader reader = Files.newBufferedReader(readerPath)) {
                writeHeaders(writer);
                Stream<T> stream = transformer.apply(stream(reader));
                stream.forEach(r -> {
                    try {
                        writeRow(r, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    /**
     * Transforms a masrshalled bean using an injected {@link Stream} operation. The input is read from reader
     * and written to the writer, Client code is responsible for closing the reader and writer.
     *
     * @param reader Input reader source
     * @param writer Output writer
     * @param transformer user supplied stream operation
     */
    @SneakyThrows
    default void transform(Reader reader, Writer writer, UnaryOperator<Stream<T>> transformer) {
        writeHeaders(writer);
        Stream<T> stream = transformer.apply(stream(reader));
        stream.forEach(r -> {
            try {
                writeRow(r, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static <T> SingleRowMarshaller<T> parser(Class<T> dataClass){
        return load(dataClass).parser();
    }

    @SneakyThrows
    static <T> void transform(
            Class<T> dataClass,
            Path readerPath,
            Path writerPath,
            UnaryOperator<Stream<T>> transformer) {
        RowMarshaller.load(dataClass).transform(readerPath, writerPath, transformer);
    }

    @SneakyThrows
    static <T> void transform(
            Class<T> dataClass,
            Reader reader,
            Writer writer,
            UnaryOperator<Stream<T>> transformer) {
        RowMarshaller.load(dataClass).transform(reader, writer, transformer);
    }

    void writeHeaders(StringBuilder builder);

    void writeInputHeaders(StringBuilder builder);

    void writeRow(T target, StringBuilder builder);

    void writeRow(T target, Writer write) throws IOException;

    void writeHeaders(Writer write) throws IOException;

    void writeInputHeaders(Writer write) throws IOException;
}
