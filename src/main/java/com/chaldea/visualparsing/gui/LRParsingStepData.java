package com.chaldea.visualparsing.gui;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.parsing.ActionItem;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.util.*;

/**
 * LR分析执行步骤表的数据
 */
public class LRParsingStepData {

    private static Grammar grammar;

    /**
     * 完整的输入符号
     */
    private static List<Terminal> inputSymbols;

    /**
     * 当前正在处理的输入符号的索引
     */
    private final int currentInputSymbolIndex;

    /**
     * 步骤编号
     */
    private final ReadOnlyIntegerWrapper number;

    /**
     * 状态栈
     */
    private final Deque<Integer> stateStack;

    /**
     * 符号栈
     */
    private final Deque<ProductionSymbol> symbolStack;

    /**
     * 动作
     */
    private final ActionItem action;

    /**
     * Instantiates a new Lr parsing step data.
     *
     * @param index       当前正在处理的输入符号的索引
     * @param number      步骤编号
     * @param stateStack  状态栈
     * @param symbolStack 符号栈
     * @param actionItem  动作
     */
    public LRParsingStepData(int index, int number, Deque<Integer> stateStack,
                             Deque<ProductionSymbol> symbolStack, ActionItem actionItem) {
        if (LRParsingStepData.grammar == null) {
            throw new BaseException("请先设置LRParsingStepData.grammar");
        }
        if (LRParsingStepData.inputSymbols == null) {
            throw new BaseException("请先设置LRParsingStepDate.inputSymbols");
        }
        this.currentInputSymbolIndex = index;
        this.number = new ReadOnlyIntegerWrapper(number);
        this.stateStack = new ArrayDeque<>(stateStack);
        this.symbolStack = new ArrayDeque<>(symbolStack);
        this.action = actionItem;
    }

    public static void setInputSymbols(List<Terminal> inputSymbols) {
        LRParsingStepData.inputSymbols = new ArrayList<>(inputSymbols);
        inputSymbols.add(Terminal.END_MARKER);
    }

    public static void setGrammar(Grammar grammar) {
        LRParsingStepData.grammar = grammar;
    }


    public SimpleStringProperty getInputSymbols() {
        List<Terminal> symbolList = inputSymbols
                .subList(currentInputSymbolIndex, inputSymbols.size());
        StringBuilder stringBuilder = new StringBuilder(32);
        for (ProductionSymbol symbol : symbolList) {
            stringBuilder.append(symbol.getValue());
        }
        return new SimpleStringProperty(stringBuilder.toString());
    }

    public ObservableValue<Integer> getNumber() {
        return number.asObject();
    }

    public ObservableValue<String> getStateStack() {
        StringBuilder stringBuilder = new StringBuilder(32);
        Iterator<Integer> iterator = stateStack.descendingIterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
        }
        return new SimpleStringProperty(stringBuilder.toString());
    }

    public ObservableValue<String> getSymbolStack() {
        StringBuilder stringBuilder = new StringBuilder(32);
        for (ProductionSymbol symbol : symbolStack) {
            stringBuilder.append(symbol.getValue());
        }
        return new SimpleStringProperty(stringBuilder.toString());
    }

    public ObservableValue<String> getAction() {
        if (action.action() == ActionItem.Action.ACCEPT) {
            return new SimpleStringProperty("接受");
        }
        if (action.action() == ActionItem.Action.SHIFT) {
            return new SimpleStringProperty("移入" + action.number());
        }
        if (action.action() == ActionItem.Action.REDUCE) {
            Production production = Grammars.getExpression(grammar, action.number());
            StringBuilder stringBuilder = new StringBuilder(32);
            stringBuilder.append("根据").append(production.getHead().getValue())
                    .append("→");
            for (ProductionSymbol symbol : production.getBody().get(0).getValue()) {
                stringBuilder.append(symbol.getValue());
            }
            stringBuilder.append("规约");
            return new SimpleStringProperty(stringBuilder.toString());
        };
        // Make compiler happy
        return null;
    }
}
