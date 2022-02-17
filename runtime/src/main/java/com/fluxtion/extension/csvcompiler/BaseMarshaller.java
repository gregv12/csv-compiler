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
    protected final char[] chars = new char[4096];
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
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        foundRecord = true;
                        break;
                    }
                }
                if (eof()) {
                    foundRecord = true;
                }
            } else {
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        validator.accept(target, this::logRowValidationProblem);
                        foundRecord = true;
                        if (passedValidation()) {
                            break;
                        }
                    }
                }
                if (eof()) {
                    validator.accept(target, this::logRowValidationProblem);
                    foundRecord = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvProcessingException processingException) {
            this.handleFatalProcessingError(processingException);
        }
        return target;
    }

    @Override
    public final void forEach(Consumer<T> consumer, Reader in) {
        init();
        int c;
        try {
            if (validator == null) {
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        consumer.accept(target);
                    }
                }
                if (eof()) {
                    consumer.accept(target);
                }
            } else {
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        validator.accept(target, this::logRowValidationProblem);
                        if (passedValidation()) {
                            consumer.accept(target);
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
        previousChar = '\0';
        firstCharOfField = true;
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
