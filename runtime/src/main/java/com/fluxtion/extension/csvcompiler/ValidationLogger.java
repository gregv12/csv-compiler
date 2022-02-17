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

/**
 * Logs validation failure messages from a {@link RowMarshaller} instance
 */
public interface ValidationLogger {
    ValidationLogger NULL = new ValidationLogger() {
        public void logFatal(CsvProcessingException csvProcessingException) {
        }

        public void logWarning(CsvProcessingException csvProcessingException) {
        }
    };

    ValidationLogger CONSOLE = new ValidationLogger() {
        public void logFatal(CsvProcessingException csvProcessingException) {
            System.out.println(csvProcessingException.getMessage());
        }

        public void logWarning(CsvProcessingException csvProcessingException) {
            System.out.println(csvProcessingException.getMessage());
        }
    };

    /**
     * log a fatal exception, processing will halt after this message
     *
     * @param csvProcessingException fatal exception
     */
    void logFatal(CsvProcessingException csvProcessingException);

    /**
     * log a warning message, processing will continue after this message is received
     *
     * @param csvProcessingException warning exception
     */
    void logWarning(CsvProcessingException csvProcessingException);


    interface FailedRowValidationProcessor {
        /**
         * Notify of a row validation failure, set a flag to induce an immediate halt of stream processing
         *
         * @param failureMessage failure message
         * @param isFatal        flag for immediate halt
         */
        void validationFailure(String failureMessage, boolean isFatal);
    }
}
