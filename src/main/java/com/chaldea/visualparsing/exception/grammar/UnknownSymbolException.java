package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;

public class UnknownSymbolException extends BaseException {
    public UnknownSymbolException() {
    }

    public UnknownSymbolException(String message) {
        super(message);
    }

    public UnknownSymbolException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownSymbolException(Throwable cause) {
        super(cause);
    }

    public UnknownSymbolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
