package com.chaldea.visualparsing.grammar;

import java.io.Serializable;
import java.util.Objects;

/**
 * 非终结符{@link com.chaldea.visualparsing.grammar.Nonterminal}与终结符
 * {@link com.chaldea.visualparsing.grammar.Terminal}的父类。
 * 用于表示产生式{@link com.chaldea.visualparsing.grammar.Production}中的{@code body}
 */
public abstract class ProductionSymbol implements Serializable {
    private final String value;

    public ProductionSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductionSymbol that = (ProductionSymbol) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
