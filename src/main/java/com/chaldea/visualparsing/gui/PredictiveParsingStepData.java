package com.chaldea.visualparsing.gui;

import com.chaldea.visualparsing.debug.PredictiveAnalyticsObserver;
import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.ProductionSymbol;
import com.chaldea.visualparsing.grammar.Terminal;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 预测分析执行步骤表的数据
 */
public class PredictiveParsingStepData {
    /**
     * 完整的输入符号
     */
    private static List<Terminal> INPUT_SYMBOLS;

    /**
     * 当前正在处理的符号的索引
     */
    private final int ip;

    /**
     * 步骤
     */
    private final SimpleStringProperty number;

    /**
     * 分析栈
     */
    private final Deque<ProductionSymbol> parsingStack;

    /**
     * 动作
     */
    private final StringProperty action;

    /**
     * 所用产生式的头
     */
    private final Nonterminal head;

    /**
     * 所用产生式的体
     */
    private final Expression expression;

    private static final Logger logger =
            LoggerFactory.getLogger(PredictiveParsingStepData.class);

    public static void setInputSymbols(List<Terminal> inputSymbols) {
        PredictiveParsingStepData.INPUT_SYMBOLS = new ArrayList<>(inputSymbols);
    }

    /**
     * Instantiates a new Predictive parsing step.
     *
     * @param ip         当前正在处理的符号的索引
     * @param number     步骤序号
     * @param stack      分析栈
     * @param action     动作
     * @param head       产生式头
     * @param expression 产生式体
     */
    public PredictiveParsingStepData(int ip, int number, Deque<ProductionSymbol> stack,
                                     String action, Nonterminal head, Expression expression) {
        this.ip = ip;
        this.number = new SimpleStringProperty(String.valueOf(number));
        this.parsingStack = new LinkedList<>(stack);
        this.action = new SimpleStringProperty(action);
        this.head = head;
        this.expression = expression;
    }

    public SimpleStringProperty getNumber() {
        return number;
    }

    public StringProperty getStack() {
        StringBuilder stringBuilder = new StringBuilder(32);
        Deque<ProductionSymbol> backupStack = new LinkedList<>(parsingStack);
        while (!parsingStack.isEmpty()) {
            stringBuilder.append(parsingStack.removeLast().getValue());
        }
        parsingStack.addAll(backupStack);
        return new SimpleStringProperty(stringBuilder.toString());
    }

    public StringProperty getInputQueue() {
        List<Terminal> symbolList = INPUT_SYMBOLS.subList(ip,
                INPUT_SYMBOLS.size() - 1);
        StringBuilder stringBuilder = new StringBuilder(32);
        for (ProductionSymbol symbol : symbolList) {
            stringBuilder.append(symbol.getValue());
        }
        return new SimpleStringProperty(stringBuilder.toString());
    }

    public StringProperty getAction() {
        return action;
    }

    public StringProperty getUsedProduction() {
        // 当action为匹配或接受时，所用产生式信息为空字符串即可
        if (PredictiveAnalyticsObserver.MATCH.equals(action.getValue()) ||
                PredictiveAnalyticsObserver.ACCEPT.equals(action.getValue())) {
            return new SimpleStringProperty("");
        }
        StringBuilder stringBuilder = new StringBuilder(32);
        stringBuilder.append(head.getValue()).append("→");
        for (ProductionSymbol symbol : expression.getValue()) {
            stringBuilder.append(symbol.getValue());
        }
        return new SimpleStringProperty(stringBuilder.toString());
    }

    @Override
    public String toString() {
        return "PredictiveParsingStepData{" +
                "ip=" + ip +
                ", number=" + number +
                ", parsingStack=" + parsingStack +
                ", action=" + action +
                ", head=" + head +
                ", expression=" + expression +
                '}';
    }
}
