package com.chaldea.visualparsing.debug;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.LRParsingException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.parsing.ActionItem;
import com.chaldea.visualparsing.parsing.LRParsingTable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * The type Lr parsing algorithm.LR语法分析算法
 * <p>其中的每一步算法对应着龙书第二版P160最上面的伪代码</p>
 */
public class LRParsingAlgorithm extends StepwiseAlgorithm {
    private LRParsingTable lrParsingTable;
    private final Deque<Integer> stateStack;
    private final Deque<ProductionSymbol> symbolStack;
    private List<Terminal> inputSymbols;
    private int state;
    private ProductionSymbol symbol;
    private int symbolIndex;

    /**
     * Instantiates a new Lr parsing algorithm.
     * 需要在后续设置lrParsingTable和input
     */
    public LRParsingAlgorithm() {
        algorithmStepList = new ArrayList<>();
        stateStack = new LinkedList<>();
        symbolStack = new LinkedList<>();
        algorithmStepList.add(letSIsThsStateOfTop());
        algorithmStepList.add(ifShift());
        algorithmStepList.add(pushTOntoStackAndChangeSymbol());
        algorithmStepList.add(elseIfReduce());
        algorithmStepList.add(popSymbolFromStackAndSetT());
        algorithmStepList.add(pushGoOntoStackAndOutputProduction());
        algorithmStepList.add(elseIfAccept());
        algorithmStepList.add(elseErrorRecovery());
    }

    public LRParsingAlgorithm(LRParsingTable lrParsingTable, List<Terminal> input) {
        this();
        setLrParsingTable(lrParsingTable);
        setInputSymbols(input);
    }

    public void setLrParsingTable(LRParsingTable lrParsingTable) {
        this.lrParsingTable = lrParsingTable;
    }

    public void setInputSymbols(List<Terminal> inputSymbols) {
        this.inputSymbols = inputSymbols;
        this.inputSymbols.add(Terminal.END_MARKER);
    }

    @Override
    public void reset() {
        currentStepIndex = 0;
        lastStepReturnValue = null;
        stateStack.clear();
        symbolStack.clear();
        // 令a为w$的第一个符号
        symbol = inputSymbols.get(0);
        stateStack.push(0);
        symbolIndex = 0;
    }

    @Override
    public void executeStep() {
        lastStepReturnValue =
                algorithmStepList.get(currentStepIndex++).execute(lastStepReturnValue);
    }

    /**
     * Let s is ths state of top algorithm step. 令s是栈顶的状态
     *
     * @return the algorithm step
     */
    private AlgorithmStep letSIsThsStateOfTop() {
        return parameters -> {
            if (stateStack.isEmpty()) {
                throw new LRParsingException("stateStack为空");
            }
            state = stateStack.peek();
            return null;
        };
    }

    /**
     * Judge is shift algorithm step.
     * <p>if(ACTION[s,a]=移入t)</p>
     *
     * @return the algorithm step
     */
    private AlgorithmStep ifShift() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state,(Terminal) symbol);
            if (actionItem.action() != ActionItem.Action.SHIFT) {
                currentStepIndex += 2;
                return null;
            }
            return new Object[] {actionItem.number()};
        };
    }

    private AlgorithmStep pushTOntoStackAndChangeSymbol() {
        return parameters -> {
            int t = (Integer) parameters[0];
            stateStack.push(t);
            symbolStack.push(symbol);
            toNextSymbol();
            currentStepIndex = 0;
            return null;
        };
    }

    private AlgorithmStep elseIfReduce() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state,(Terminal) symbol);
            if (actionItem.action() != ActionItem.Action.REDUCE) {
                currentStepIndex += 3;
                return null;
            }
            return new Object[] {actionItem};
        };
    }

    private AlgorithmStep popSymbolFromStackAndSetT() {
        return parameters -> {
            int expressionIndex = ((ActionItem) parameters[0]).number();
            Production production =
                    Grammars.getExpression(lrParsingTable.getGrammar(), expressionIndex);
            for (int i = 0; i < production.getBody().get(0).length(); ++i) {
                symbolStack.pop();
                stateStack.pop();
            }
            return new Object[] {production};
        };
    }

    private AlgorithmStep pushGoOntoStackAndOutputProduction() {
        return parameters -> {
            if (stateStack.isEmpty()) {
                throw new LRParsingException("stateStack为空");
            }
            Production production = (Production) parameters[0];
            int newState = lrParsingTable.go(stateStack.peek(), production.getHead());
            stateStack.push(newState);
            symbolStack.push(production.getHead());
            currentStepIndex = 0;
            return null;
        };
    }

    private AlgorithmStep elseIfAccept() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state, (Terminal) symbol);
            if (actionItem.action() == ActionItem.Action.ACCEPT) {
                completeExecution();
            }
            return null;
        };
    }

    private AlgorithmStep elseErrorRecovery() {
        return parameters -> {
            throw new LRParsingException();
        };
    }

    /**
     * Change symbol.
     *
     * @param index the index
     */
    private void changeSymbol(int index) {
        symbolIndex = index;
        symbol = inputSymbols.get(symbolIndex);
    }

    /**
     * To next symbol.
     */
    private void toNextSymbol() {
        changeSymbol(symbolIndex + 1);
    }

    private void completeExecution() {
        currentStepIndex = algorithmStepList.size();
    }
}
