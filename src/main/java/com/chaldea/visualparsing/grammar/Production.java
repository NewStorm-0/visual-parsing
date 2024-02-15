package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.grammar.EmptyHeadProductionException;
import com.chaldea.visualparsing.exception.grammar.RepeatedProductionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 产生式
 */
public class Production implements Serializable {
    /**
     * 产生式头
     */
    private Nonterminal head;

    /**
     * 产生式体
     * <p>该处可以改为 {@code Set<Expression>}，能使一些方法代码更加简洁。但在目前
     * 阶段哪一种更适合是未知的</p>
     */
    private List<Expression> body;

    public Production(Nonterminal head) {
        this.head = head;
        body = new ArrayList<>();
    }

    public Production(Nonterminal head, List<Expression> body) {
        this.head = head;
        this.body = body;
    }

    public Nonterminal getHead() {
        return head;
    }

    public void setHead(Nonterminal head) {
        this.head = head;
    }

    public List<Expression> getBody() {
        return body;
    }

    public void setBody(List<Expression> body) {
        this.body = body;
    }

    /**
     * 判断产生式是否为空
     * @return 若 {@code head} 为 {@code null}，则返回 {@code true}
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * 向产生式体中增加一个 {@link com.chaldea.visualparsing.grammar.Expression}
     * @param exp
     */
    public void addExpression(Expression exp) {
        if (isEmpty()) {
            throw new EmptyHeadProductionException();
        }
        for (Expression exp1 : body) {
            if (exp1.equals(exp)) {
                throw new RepeatedProductionException();
            }
        }
        body.add(exp);
    }

    /**
     * 向产生式体中删掉一个 {@link com.chaldea.visualparsing.grammar.Expression}
     * @param exp
     */
    public void eraseExpression(Expression exp) {
        body.remove(exp);
    }

    /**
     * 删除产生式体中指定索引处的 {@link com.chaldea.visualparsing.grammar.Expression}
     *
     * @param index 要删除的表达式的索引
     */
    public void eraseExpression(int index) {
        body.remove(index);
    }

    /**
     * 修改产生式中的一个 {@link com.chaldea.visualparsing.grammar.Expression}
     * @param index 要修改的 {@code expression} 的索引
     * @param exp 新的表达式
     */
    public void modifyExpression(int index, Expression exp) {
        for (Expression expression : body) {
            if (expression.equals(exp)) {
                throw new RepeatedProductionException(expression);
            }
        }
        body.set(index, exp);
    }

    /**
     * Modify expression.
     *
     * @param oldExpression the old expression
     * @param newExpression the new expression
     */
    public void modifyExpression(Expression oldExpression, Expression newExpression) {
        int index = body.indexOf(oldExpression);
        if (index == -1) {
            throw new BaseException("不存在的旧表达式" + "：" + head + " → " + oldExpression);
        }
        modifyExpression(index, newExpression);
    }

    @Override
    public String toString() {
        return "Production{" +
                "head=" + head +
                ", body=" + Arrays.toString(body.toArray()) +
                '}';
    }
}
