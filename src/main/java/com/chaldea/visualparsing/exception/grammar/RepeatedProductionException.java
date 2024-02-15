package com.chaldea.visualparsing.exception.grammar;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.grammar.Expression;

public class RepeatedProductionException extends BaseException {
    public RepeatedProductionException() {}

    public RepeatedProductionException(Expression expression) {
        super(expression.toString() + "已经存在");
    }
}
