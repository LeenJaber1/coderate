package com.coderate.backend.exceptions;

public class ConflictOccurredException extends Exception{
    public ConflictOccurredException() {
    }

    public ConflictOccurredException(String message) {
        super(message);
    }

    public ConflictOccurredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictOccurredException(Throwable cause) {
        super(cause);
    }

    public ConflictOccurredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
