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

public abstract class BaseMarshallerNoBufferCopy<T> implements RowMarshaller<T> {

    protected int rowNumber;
    protected T target;
    protected final boolean failOnError;
    protected final HashMap<Integer, String> fieldMap = new HashMap<>();
    protected ValidationLogger errorLog = ValidationLogger.CONSOLE;
    private static final int READ_SIZE = 8192;
    protected final char[] chars = new char[READ_SIZE * 2];
    protected final int[] delimiterIndex = new int[1024];
    protected final CharArrayCharSequence sequence = new CharArrayCharSequence(chars);

    //the last read point of the buffer
    protected int readPointer = 0;
    //the last index in the buffer data was written to
    protected int writtenLimit = -1;
    //chars read pointers
    protected int fieldIndex = 0;
    protected boolean emptyRow = true;
    protected char previousChar = '\0';
    protected boolean firstCharOfField = true;
    protected int maxFieldIndex = 0;

    protected BiConsumer<T, FailedRowValidationProcessor> validator;
    protected Function<String, String> headerTransformer = Function.identity();
    protected boolean passedValidation;
    protected Consumer<CsvProcessingException> fatalExceptionHandler;
    private boolean foundRecord;
    protected boolean publish;
    protected StringBuilder builder = new StringBuilder(8192);

    protected BaseMarshallerNoBufferCopy(boolean failOnError) {
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

    private int queueUnreadCharacters() {
        int oldReadPointer = readPointer;
        if (writtenLimit == readPointer) {
                int offset = delimiterIndex[0];
                for (int i = 0; i <= fieldIndex; i++) {
                    delimiterIndex[i] = delimiterIndex[i] - offset;
                    readPointer = delimiterIndex[i];
                }
                System.arraycopy(chars, offset, chars, 0, chars.length - offset);
                oldReadPointer -= offset;
        }
        return oldReadPointer;
    }

    private T next(Reader in) {
        foundRecord = false;
        try {
            if (validator == null) {
                //clear unprocessed
                for (; readPointer < writtenLimit; readPointer++) {
                    if (charEvent(chars[readPointer])) {
                        foundRecord = true;
                        readPointer++;
                        return target;
                    }
                }
                //shift any unread indexes to front of buffer and reset pointers
                int oldReadPointer = queueUnreadCharacters();
                //consume from reader
                while ((writtenLimit = in.read(chars, oldReadPointer, chars.length - oldReadPointer)) != -1) {
                    writtenLimit = oldReadPointer + writtenLimit;
                    for (; readPointer < writtenLimit; readPointer++) {
                        if (charEvent(chars[readPointer])) {
                            foundRecord = true;
                            readPointer++;
                            return target;
                        }
                    }
                }
                if (oldReadPointer > readPointer) {
                    readPointer = oldReadPointer;
                }
                if (eof()) {
                    foundRecord = true;
                }
            } else {
                //clear unprocessed
                for (; readPointer < writtenLimit; readPointer++) {
                    if (charEvent(chars[readPointer])) {
                        validator.accept(target, this::logRowValidationProblem);
                        foundRecord = false;
                        readPointer++;
                        if (passedValidation()) {
                            foundRecord = true;
                            return target;
                        }
                    }
                }
                //shift any unread indexes to front of buffer and reset pointers
                int oldReadPointer = queueUnreadCharacters();//readPointer;
                //consume from reader
                while ((writtenLimit = in.read(chars, oldReadPointer, chars.length - oldReadPointer)) != -1) {
                    writtenLimit = oldReadPointer + writtenLimit;
                    for (; readPointer < writtenLimit; readPointer++) {
                        if (charEvent(chars[readPointer])) {
                            validator.accept(target, this::logRowValidationProblem);
                            foundRecord = false;
                            readPointer++;
                            if (passedValidation()) {
                                foundRecord = true;
                                return target;
                            }
                        }
                    }
                }
                if (oldReadPointer > readPointer) {
                    readPointer = oldReadPointer;
                }
                if (eof()) {
                    validator.accept(target, this::logRowValidationProblem);
                    foundRecord = passedValidation();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvProcessingException processingException) {
            delimiterIndex[fieldIndex] = readPointer + 1;
            this.handleFatalProcessingError(processingException);
        }
        return target;
    }

    @Override
    public final void forEach(Consumer<? super T> consumer, Reader in) {
        init();
        readPointer = 0;
        int oldReadPointer = 0;
        try {
            if (validator == null) {
                //buffer read
                while ((writtenLimit = in.read(chars, oldReadPointer, chars.length - oldReadPointer)) != -1) {
                    writtenLimit = oldReadPointer + writtenLimit;
                    boolean readAll = writtenLimit == chars.length;
                    for (; readPointer < writtenLimit; ) {
                        if (charEvent(chars[readPointer])) {
                            consumer.accept(target);
                        }
                        readPointer++;
                    }
                    if (readAll) {
                        oldReadPointer = queueUnreadCharacters();
                    }
                }
                if (eof()) {
                    consumer.accept(target);
                }
            } else {
                while ((writtenLimit = in.read(chars, oldReadPointer, chars.length - oldReadPointer)) != -1) {
                    writtenLimit = oldReadPointer + writtenLimit;
                    boolean readAll = writtenLimit == chars.length;
                    for (; readPointer < writtenLimit; ) {
                        if (charEvent(chars[readPointer])) {
                            validator.accept(target, this::logRowValidationProblem);
                            if (passedValidation()) {
                                consumer.accept(target);
                            }
                        }
                        readPointer++;
                    }
                    if(readAll){
                        oldReadPointer = queueUnreadCharacters();
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

    protected boolean validateField(java.util.function.BiPredicate<T, BiConsumer<String, Boolean>> rowValidator){
        return rowValidator.test(target, this::logFieldValidationProblem);
    }

    protected final void logRowValidationProblem(String errorMessage, boolean isFatal) {
        passedValidation = false;
        String msg = errorMessage + " validation problem line:" + getRowNumber();
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
        String msg = "\n" + errorMessage + " validation problem line:" + getRowNumber();

        msg += " fieldIndex:'"
                + fieldIndex
                + "' targetMethod:'" + targetClass().getSimpleName() + "#"
                + fieldMap.get(fieldIndex) + "'"
                + "\ninput : '" + sequence.subSequence(delimiterIndex[fieldIndex], delimiterIndex[fieldIndex+1]) + "'"
                + "\noutput: " + target.toString()
//                +"\n"
        ;

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
        emptyRow = true;
//        writeIndex = 0;
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
        if (!emptyRow) {
            if (isEscaping() && !isPreviousAQuote()) {
                charEvent('\"');
                readPointer++;
                return charEvent('\n');
            } else if (isEscaping() || isPreviousAQuote()) {
                return charEvent('\n');
            }
            return processRow();
        }
        return false;
    }

    protected boolean isEscaping() {
        return false;
    }

    protected boolean isPreviousAQuote() {
        return false;
    }

    protected final void updateFieldIndex() {
        firstCharOfField = true;
        fieldIndex++;
        delimiterIndex[fieldIndex] = readPointer + 1;
    }

    protected void removeCharFromBuffer() {
        writtenLimit--;
        System.arraycopy(
                chars, readPointer + 1,
                chars, readPointer, chars.length - readPointer - 1);
        readPointer--;
    }

    protected void removeCharFromBuffer(int offSet) {
        writtenLimit--;
        System.arraycopy(
                chars, readPointer + 1 - offSet,
                chars, readPointer - offSet, chars.length - readPointer - 1 - offSet);
        readPointer--;
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
                BaseMarshallerNoBufferCopy.this.next(in);
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
