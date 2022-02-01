package com.fluxtion.extension.csvcompiler;

public class CsvProcessingException extends RuntimeException {

    final int lineNumber;

    public CsvProcessingException(String reason, Throwable cause, int lineNumber) {
        super(reason, cause);
        this.lineNumber = lineNumber;
    }

    public CsvProcessingException(String reason, int lineNumber) {
        super(reason);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
