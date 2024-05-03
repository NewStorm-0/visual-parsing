package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;

import java.util.Arrays;
import java.util.Objects;

public class LR1Item extends Item {

    /**
     * The Lookahead symbol.向前看符号
     */
    private final Terminal lookahead;

    public LR1Item(Nonterminal head, Expression expression, int point,
                   Terminal lookahead) {
        super(head, expression, point);
        this.lookahead = lookahead;
    }

    public Terminal getLookahead() {
        return lookahead;
    }

    /**
     * 判断第一分量是否相同
     *
     * @param lr1Item the lr 1 item
     * @return the boolean
     */
    public boolean itemEquals(LR1Item lr1Item) {
        return super.equals(lr1Item);
    }

    /**
     * 获取第一分量，是一个Item对象
     *
     * @return the item
     */
    public Item getItem() {
        return new Item(getHead(), getExpression(), getPoint());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LR1Item lr1Item = (LR1Item) o;
        return Objects.equals(lookahead, lr1Item.lookahead);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lookahead);
    }

    @Override
    public String toString() {
        return super.toString() + "," + lookahead.getValue();
    }
}
