package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;

public class RepeatedSymbolException extends BaseException {
    public RepeatedSymbolException() {
        super();
    }

    public RepeatedSymbolException(String message) {
        super(message);
    }

    public RepeatedSymbolException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatedSymbolException(Throwable cause) {
        super(cause);
    }

    public RepeatedSymbolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
