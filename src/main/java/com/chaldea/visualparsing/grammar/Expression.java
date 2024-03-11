package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.grammar.IllegalSymbolException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 表达式，是产生式体中的一条
 */
public class Expression implements Serializable, Cloneable {
    private ProductionSymbol[] value;

    @Deprecated
    public Expression() {
        value = new ProductionSymbol[]{};
    }

    public Expression(ProductionSymbol[] value) {
        setValue(value);
    }

    public Expression(List<ProductionSymbol> value) {
        setValue(value.toArray(ProductionSymbol[]::new));
    }

    public ProductionSymbol[] getValue() {
        return value;
    }

    public void setValue(ProductionSymbol[] value) {
        if (value == null) {
            throw new BaseException("Expression.value 为空");
        }
        if (value.length == 0) {
            throw new BaseException("Expression.value.length 为 0");
        }
        if (Arrays.stream(value).toList().contains(Terminal.EMPTY_STRING) && value.length > 1) {
            throw new IllegalSymbolException("Expression.value含有空串时不能有其它符号, value:" + Arrays.toString(value));
        }
        this.value = value;
    }

    public List<ProductionSymbol> getValueList() {
        return Arrays.stream(value).toList();
    }

    /**
     * 获取索引为 index 的文法符号
     *
     * @param index 索引
     * @return {@code value} 中对应索引的值
     */
    public ProductionSymbol get(int index) {
        return value[index];
    }

    public boolean isEmptyString() {
        return value.length == 1 && value[0].equals(Terminal.EMPTY_STRING);
    }

    public Expression copy() {
        try {
            return (Expression) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public boolean isEmpty() {
        // TODO: 如何处理表达式体为空
        return value == null || value.length == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Expression that = (Expression) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return "Expression{" +
                "value=" + Arrays.toString(value) +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Expression expression = (Expression) super.clone();
        expression.setValue(value.clone());
        return expression;
    }
}
