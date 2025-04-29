package com.coderate.backend.exceptions;

public class MissingSessionIdException extends Exception{
    public MissingSessionIdException() {
        super();
    }

    public MissingSessionIdException(String message) {
        super(message);
    }

    public MissingSessionIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingSessionIdException(Throwable cause) {
        super(cause);
    }

    protected MissingSessionIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
