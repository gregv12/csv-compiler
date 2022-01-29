package com.fluxtion.extension.csvcompiler;

public interface ValidationLogger {
    ValidationLogger NULL = new ValidationLogger() {
        @Override
        public void logFatal(CharSequence error) {
        }

        @Override
        public void logException(CharSequence error) {
        }
    };

    ValidationLogger CONSOLE = new ValidationLogger() {
        @Override
        public void logFatal(CharSequence error) {
            System.out.println("validation error:" + error);
        }

        @Override
        public void logException(CharSequence error) {
            System.out.println("validation exception:" + error);
        }
    };

    void logFatal(CharSequence error);
    void logException(CharSequence error);
}
