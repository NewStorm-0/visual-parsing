package com.chaldea.visualparsing.exception;

public class IllegalItemPointException extends BaseException{
    public IllegalItemPointException() {
    }

    public IllegalItemPointException(String message) {
        super(message);
    }

    public IllegalItemPointException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalItemPointException(Throwable cause) {
        super(cause);
    }

    public IllegalItemPointException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
