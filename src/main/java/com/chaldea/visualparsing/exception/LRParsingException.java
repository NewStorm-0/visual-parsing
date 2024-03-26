package com.chaldea.visualparsing.exception;

public class LRParsingException extends BaseException{
    public LRParsingException() {
    }

    public LRParsingException(String message) {
        super(message);
    }

    public LRParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LRParsingException(Throwable cause) {
        super(cause);
    }

    public LRParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
