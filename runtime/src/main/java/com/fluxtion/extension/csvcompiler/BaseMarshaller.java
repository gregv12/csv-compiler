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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseMarshaller<T> implements RowMarshaller<T> {

    protected int rowNumber;
    protected T target;
    protected final boolean failOnError;
    protected final HashMap<Integer, String> fieldMap = new HashMap<>();
    protected ValidationLogger errorLog = ValidationLogger.CONSOLE;
    private static final int READ_SIZE = 8192;
    protected final char[] chars = new char[READ_SIZE*2];
    protected final int[] delimiterIndex = new int[1024];
    protected final CharArrayCharSequence sequence = new CharArrayCharSequence(chars);
    protected int fieldIndex = 0;
    protected int writeIndex = 0;
    protected BiConsumer<T, FailedRowValidationProcessor> validator;
    protected Function<String, String> headerTransformer = Function.identity();
    protected char previousChar = '\0';
    protected boolean firstCharOfField = true;
    protected boolean passedValidation;
    protected Consumer<CsvProcessingException> fatalExceptionHandler;
    private boolean foundRecord;
    private final char[] buf = new char[READ_SIZE];
    int readPointer = 0;
    int writtenLimit = -1;
    protected StringBuilder builder = new StringBuilder(8192);

    protected BaseMarshaller(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public final RowMarshaller<T> setValidationLogger(ValidationLogger errorLog) {
        this.errorLog = errorLog;
        return this;
    }

    public final RowMarshaller<T> setFatalExceptionHandler(Consumer<CsvProcessingException> fatalExceptionHandler) {
        this.fatalExceptionHandler = fatalExceptionHandler;
        return this;
    }

    @Override
    public Iterator<T> iterator(Reader in) {
        init();
        foundRecord = false;
        return new MyIterator(in);
    }

    @Override
    public Stream<T> stream(Reader in) {
        Iterable<T> iterable = () -> iterator(in);
        Spliterator<T> spliterator = iterable.spliterator();
        return StreamSupport.stream(spliterator, false);
    }

    private T next(Reader in) {
        int c;
        foundRecord = false;
        try {
            if (validator == null) {
                //clear unprocessed
                for (; readPointer < writtenLimit; readPointer++) {
                    if (charEvent(buf[readPointer])) {
                        foundRecord = true;
                        readPointer++;
                        return target;
                    }
                }
                if(writtenLimit==readPointer){
                    writtenLimit = -1;
                    readPointer = 0;
                }
                //consume from reader
                while ((c = in.read(buf, readPointer, buf.length - readPointer)) != -1) {
                    writtenLimit = readPointer + c;
                    for (; readPointer < c; readPointer++) {
                        if (charEvent(buf[readPointer])) {
                            foundRecord = true;
                            readPointer++;
                            return target;
                        }
                    }
                }
                if (eof()) {
                    foundRecord = true;
                }
            } else {
                    for (; readPointer < writtenLimit; readPointer++) {
                        if (charEvent(buf[readPointer])) {
                            validator.accept(target, this::logRowValidationProblem);
                            foundRecord = true;
                            readPointer++;
                            if(passedValidation()){
                                return target;
                            }
                        }
                    }
                    //consume from reader
                    while ((c = in.read(buf, readPointer, buf.length - readPointer)) != -1) {
                        writtenLimit = readPointer + c;
                        for (; readPointer < c; readPointer++) {
                            if (charEvent(buf[readPointer])) {
                                validator.accept(target, this::logRowValidationProblem);
                                foundRecord = true;
                                readPointer++;
                                if(passedValidation()){
                                    return target;
                                }
                            }
                        }
                    }
                }
                if (eof()) {
                    validator.accept(target, this::logRowValidationProblem);
                    foundRecord = true;
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvProcessingException processingException) {
            this.handleFatalProcessingError(processingException);
        }
        return target;
    }

    @Override
    public final void forEach(Consumer<? super T> consumer, Reader in) {
        init();
        int c;
        try {
            if (validator == null) {
                //buffer read
                while ((c = in.read(buf)) != -1) {
                    for (int i = 0; i < c; i++) {
                        if (charEvent(buf[i])) {
                            consumer.accept(target);
                        }
                    }
                }

                if (eof()) {
                    consumer.accept(target);
                }
            } else {
                while ((c = in.read(buf)) != -1) {
                    for (int i = 0; i < c; i++) {
                        if (charEvent(buf[i])) {
                            validator.accept(target, this::logRowValidationProblem);
                            if (passedValidation()) {
                                consumer.accept(target);
                            }
                        }
                    }
                }

                if (eof()) {
                    validator.accept(target, this::logRowValidationProblem);
                    if (passedValidation()) {
                        consumer.accept(target);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvProcessingException processingException) {
            this.handleFatalProcessingError(processingException);
        }
    }

    @Override
    public final RowMarshaller<T> setHeaderTransformer(Function<String, String> headerTransformer) {
        this.headerTransformer = headerTransformer;
        return this;
    }

    @Override
    public final RowMarshaller<T> setRowValidator(BiConsumer<T, FailedRowValidationProcessor> validator) {
        this.validator = validator;
        return this;
    }

    protected void handleFatalProcessingError(CsvProcessingException exception) {
        if (fatalExceptionHandler == null) {
            throw exception;
        } else {
            fatalExceptionHandler.accept(exception);
        }
    }

    protected boolean validate(int value, java.util.function.IntPredicate predicate, String errorMessage, boolean failFast) {
        if (!predicate.test(value)) {
            logFieldValidationProblem(errorMessage + " value:'" + value + "'", failFast);
            return false;
        }
        return true;
    }

    protected boolean validate(double value, java.util.function.DoublePredicate predicate, String errorMessage, boolean failFast) {
        if (!predicate.test(value)) {
            logFieldValidationProblem(errorMessage + " value:'" + value + "'", failFast);
            return false;
        }
        return true;
    }

    protected boolean validate(long value, java.util.function.LongPredicate predicate, String errorMessage, boolean failFast) {
        if (!predicate.test(value)) {
            logFieldValidationProblem(errorMessage + " value:'" + value + "'", failFast);
            return false;
        }
        return true;
    }

    protected boolean validate(T value, java.util.function.Predicate<T> predicate, String errorMessage, boolean failFast) {
        if (!predicate.test(value)) {
            logFieldValidationProblem(errorMessage + " value:'" + value + "'", failFast);
            return false;
        }
        return true;
    }

    protected final void logRowValidationProblem(String errorMessage, boolean isFatal) {
        passedValidation = false;
        String msg = "Validation problem line:" + getRowNumber() + " " + errorMessage;
        CsvProcessingException exception = new CsvProcessingException(msg, getRowNumber());
        if (isFatal) {
            errorLog.logFatal(exception);
            throw exception;
        } else {
            errorLog.logWarning(exception);
        }
    }

    protected final void logFieldValidationProblem(String errorMessage, boolean failFast) {
        passedValidation = false;
        String msg = "Validation problem line:" + getRowNumber() + " " + errorMessage;

        msg += " fieldIndex:'"
                + fieldIndex
                + "' targetMethod:'" + targetClass().getSimpleName() + "#"
                + fieldMap.get(fieldIndex) + "'";

        CsvProcessingException exception = new CsvProcessingException(msg, getRowNumber());
        if (failOnError || failFast) {
            errorLog.logFatal(exception);
            throw exception;
        } else {
            errorLog.logWarning(exception);
        }
    }

    protected abstract boolean charEvent(char c);

    protected void init() {
        fieldIndex = 0;
        writeIndex = 0;
        rowNumber = 0;
        readPointer = 0;
        writtenLimit = -1;
        previousChar = '\0';
        firstCharOfField = true;
        builder.setLength(0);
    }

    public void writeRow(T target, Writer write) throws IOException {
        builder.setLength(0);
        writeRow(target, builder);
        write.append(builder);
    }

    public void writeHeaders(Writer write) throws IOException {
        builder.setLength(0);
        writeHeaders(builder);
        write.append(builder);
    }

    public void writeInputHeaders(Writer write) throws IOException {
        builder.setLength(0);
        writeInputHeaders(builder);
        write.append(builder);
    }

    protected abstract boolean processRow();

    protected final boolean passedValidation() {
        return passedValidation;
    }

    protected final int getRowNumber() {
        return rowNumber;
    }

    protected final boolean eof() {
        if (writeIndex != 0) {
            return processRow();
        }
        return false;
    }

    protected final void updateFieldIndex() {
        firstCharOfField = true;
        fieldIndex++;
        delimiterIndex[fieldIndex] = writeIndex + 1;
    }

    protected final void logException(String prefix, boolean fatal, Exception e) {
        String sb = targetClass().getSimpleName() + " "
                + prefix
                + " fieldIndex:'"
                + fieldIndex
                + "' targetMethod:'" + targetClass().getSimpleName() + "#"
                + fieldMap.get(fieldIndex)
                + "' error:'"
                + e.toString()
                + "'";
        CsvProcessingException csvProcessingException
                = new CsvProcessingException(sb, e, rowNumber);
        if (fatal || failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logWarning(csvProcessingException);
    }

    protected final void logProblem(String description) {
        CsvProcessingException csvProcessingException
                = new CsvProcessingException(description, rowNumber);
        if (failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logWarning(csvProcessingException);
    }

    protected final void logHeaderProblem(String prefix, boolean fatal, Exception e) {
        String sb = "Header problem for " + targetClass().getSimpleName()
                + " " + prefix + rowNumber;
        CsvProcessingException csvProcessingException
                = new CsvProcessingException(sb, e, rowNumber);
        if (fatal || failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logWarning(csvProcessingException);
    }

    private static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    class MyIterator implements Iterator<T> {

        private final Reader in;

        public MyIterator(Reader in) {
            this.in = in;
        }

        @Override
        public boolean hasNext() {
            if (!foundRecord) {
                BaseMarshaller.this.next(in);
            }
            return foundRecord;
        }

        @Override
        public T next() {
            hasNext();
            foundRecord = false;
            return target;
        }
    }
}
