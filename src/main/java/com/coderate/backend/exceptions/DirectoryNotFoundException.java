package com.coderate.backend.exceptions;

public class DirectoryNotFoundException extends Exception{
    public DirectoryNotFoundException() {
        super();
    }

    public DirectoryNotFoundException(String message) {
        super(message);
    }

    public DirectoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryNotFoundException(Throwable cause) {
        super(cause);
    }

    protected DirectoryNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
