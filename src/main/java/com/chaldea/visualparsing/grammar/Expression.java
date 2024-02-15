package com.chaldea.visualparsing.grammar;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 表达式，是产生式体中的一条
 */
public class Expression implements Serializable {
    private ProductionSymbol[] value;

    public Expression() {
        value = new ProductionSymbol[] {};
    }

    public Expression(ProductionSymbol[] value) {
        setValue(value);
    }

    public ProductionSymbol[] getValue() {
        return value;
    }

    public void setValue(ProductionSymbol[] value) {
        this.value = value;
    }

    /**
     * 获取索引为 index 的文法符号
     * @param index 索引
     * @return {@code value} 中对应索引的值
     */
    public ProductionSymbol get(int index) {
        return value[index];
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
    public String toString() {
        return "Expression{" +
                "value=" + Arrays.toString(value) +
                '}';
    }
}
