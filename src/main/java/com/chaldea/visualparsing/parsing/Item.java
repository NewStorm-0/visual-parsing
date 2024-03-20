package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.IllegalItemPointException;
import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.ProductionSymbol;

import java.util.Objects;

/**
 * 项
 */
public class Item {
    /**
     * 产生式头
     */
    private final Nonterminal head;

    /**
     * 产生式体，只有一条，所以是一个Expression对象
     */
    private final Expression expression;

    /**
     * 点的位置。
     *
     * <p>例如A→X·Y中，点的位置为1</p>
     */
    private int point;

    public Item(Nonterminal head, Expression expression, int point) {
        this.head = head;
        this.expression = expression;
        setPoint(point);
    }

    public Nonterminal getHead() {
        return head;
    }

    public Expression getExpression() {
        return expression;
    }

    public int getPoint() {
        return point;
    }

    /**
     * Gets current symbol.
     *
     * @return the current symbol
     */
    public ProductionSymbol getCurrentSymbol() {
        if (point == expression.length()) {
            return null;
        }
        return expression.get(point);
    }

    /**
     * Sets point.
     *
     * @param number the number
     */
    private void setPoint(int number) {
        if (number < 0 || number > expression.getValue().length) {
            throw new IllegalItemPointException("Item point " + number + "is not " +
                    "between 0 and " + expression.getValue().length);
        }
        point = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return getPoint() == item.getPoint() && Objects.equals(getHead(), item.getHead()) && Objects.equals(getExpression(), item.getExpression());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHead(), getExpression(), getPoint());
    }
}
