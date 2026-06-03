package com.app.playerservicejava.exception;

public abstract class PlayerBaseException extends RuntimeException {
    private final String errorCode;

    protected PlayerBaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected PlayerBaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
