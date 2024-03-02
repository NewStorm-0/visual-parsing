package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;

public class ProductionNotFoundException extends BaseException {
    public ProductionNotFoundException() {
    }

    public ProductionNotFoundException(String message) {
        super(message);
    }

    public ProductionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductionNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProductionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
