package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;

public class EmptyExpressionException extends BaseException {
    public EmptyExpressionException() {
    }

    public EmptyExpressionException(String message) {
        super(message);
    }

    public EmptyExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
