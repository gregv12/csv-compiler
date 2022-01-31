package com.fluxtion.extension.csvcompiler;

public interface ValidationLogger {
    ValidationLogger NULL = new ValidationLogger() {
        public void logFatal(CsvProcessingException csvProcessingException) {
        }

        public void logException(CsvProcessingException csvProcessingException) {
        }
    };

    ValidationLogger CONSOLE = new ValidationLogger() {
        public void logFatal(CsvProcessingException csvProcessingException) {
            System.out.print(csvProcessingException.getMessage());
        }

        public void logException(CsvProcessingException csvProcessingException) {
            System.out.print(csvProcessingException.getMessage());
        }
    };


    void logFatal(CsvProcessingException csvProcessingException);

    void logException(CsvProcessingException csvProcessingException);
}
