package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;

public class IllegalSymbolException extends BaseException {
    public IllegalSymbolException() {
    }

    public IllegalSymbolException(String message) {
        super(message);
    }

    public IllegalSymbolException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalSymbolException(Throwable cause) {
        super(cause);
    }

    public IllegalSymbolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
