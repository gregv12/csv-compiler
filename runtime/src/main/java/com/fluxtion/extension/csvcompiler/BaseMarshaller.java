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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseMarshaller<T> implements CsvMarshallerLoader<T> {

    protected int rowNumber;
    protected T target;
    protected final boolean failOnError;
    protected final HashMap<Integer, String> fieldMap = new HashMap<>();
    protected boolean passedValidation;
    protected ValidationLogger errorLog = ValidationLogger.CONSOLE;
    protected final char[] chars = new char[4096];
    protected final int[] delimiterIndex = new int[1024];
    protected StringBuilder messageSink = new StringBuilder(256);
    protected final CharArrayCharSequence sequence = new CharArrayCharSequence(chars);
    protected int fieldIndex = 0;
    protected int writeIndex = 0;
    protected BiConsumer<T, ValidationResultStore> validator;
    protected boolean failedValidation;

    protected BaseMarshaller(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public final CsvMarshallerLoader<T> setErrorLog(ValidationLogger errorLog) {
        this.errorLog = errorLog;
        return this;
    }

    @Override
    public final void stream(Consumer<T> consumer, Reader in) {
        init();
        int c;
        try {
            if(validator == null){
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        consumer.accept(target);
                    }
                }
            }else{
                failedValidation = false;
                while ((c = in.read()) != -1) {
                    if (charEvent((char) c)) {
                        validator.accept(target, this::logValidationProblem);
                        if(!failedValidation){
                            consumer.accept(target);
                        }
                    }
                }
            }
            eof();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CsvMarshallerLoader<T> setValidator(BiConsumer<T, ValidationResultStore> validator) {
        this.validator = validator;
        return this;
    }

    protected void logValidationProblem(String errorMessage){
        failedValidation = true;
        String msg = "Validation problem line:" + getRowNumber() + " " + errorMessage;
        CsvProcessingException exception = new CsvProcessingException(msg, getRowNumber());
        if(failOnError){
            errorLog.logFatal(exception);
            throw exception;
        }else{
            errorLog.logException(exception);
        }
    }

    protected abstract boolean charEvent(char c);

    protected abstract void init();

    protected abstract boolean processRow();

    protected final boolean passedValidation() {
        return passedValidation;
    }

    protected final int getRowNumber() {
        return rowNumber;
    }

    protected final void eof() {
        if (writeIndex != 0) processRow();
    }

    protected final void updateFieldIndex() {
        fieldIndex++;
        delimiterIndex[fieldIndex] = writeIndex + 1;
    }

    protected final void logException(String prefix, boolean fatal, Exception e) {
        String sb = targetClass().getSimpleName() + " " +
                prefix +
                " fieldIndex:'" +
                fieldIndex +
                "' targetMethod:'" + targetClass().getSimpleName() + "#" +
                fieldMap.get(fieldIndex) +
                "' error:'" +
                e.toString() +
                "'";
        CsvProcessingException csvProcessingException =
                new CsvProcessingException(sb, e, rowNumber);
        if (fatal || failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logException(csvProcessingException);
    }

    protected final void logProblem(String description) {
        CsvProcessingException csvProcessingException =
                new CsvProcessingException(description, rowNumber);
        if (failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logException(csvProcessingException);
    }

    protected final void logHeaderProblem(String prefix, boolean fatal, Exception e) {
        String sb = "Header problem for " + targetClass().getSimpleName() +
                " " + prefix + rowNumber;
        CsvProcessingException csvProcessingException =
                new CsvProcessingException(sb, e, rowNumber);
        if (fatal || failOnError) {
            errorLog.logFatal(csvProcessingException);
            throw csvProcessingException;
        }
        errorLog.logException(csvProcessingException);
    }
}
