package com.chaldea.visualparsing.debug;

import com.chaldea.visualparsing.exception.LRParsingException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.parsing.ActionItem;
import com.chaldea.visualparsing.parsing.LRParsingTable;

import java.util.*;

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

    /**
     * 当前处理的符号
     */
    private Terminal symbol;

    /**
     * symbol 在输入串中的索引
     */
    private int symbolIndex;

    private final List<LRParsingObserver> observers;

    private LRParsingAlgorithm() {
        observers = new ArrayList<>();
        algorithmStepList = new ArrayList<>();
        stateStack = new LinkedList<>();
        symbolStack = new LinkedList<>();
        algorithmStepList.add(letSIsThsStateOfTop());
        algorithmStepList.add(ifShift());
        algorithmStepList.add(pushTOntoStack());
        algorithmStepList.add(letABeTheNextInputSymbol());
        algorithmStepList.add(elseIfReduce());
        algorithmStepList.add(popSymbolFromStack());
        algorithmStepList.add(letTBeTheStateOfTheTopOfTheStack());
        algorithmStepList.add(pushGoOntoStack());
        algorithmStepList.add(outputProduction());
        algorithmStepList.add(elseIfAccept());
        algorithmStepList.add(elseErrorRecovery());
    }

    /**
     * Instantiates a new Lr parsing algorithm.
     *
     * @param lrParsingTable LR语法分析表
     * @param input          输入符号
     */
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
        observers.forEach(observer -> {
            observer.showNextAlgorithmStep(0);
            observer.initializeParserState(0);
        });
    }

    @Override
    public void executeStep() {
        lastStepReturnValue =
                algorithmStepList.get(currentStepIndex++).execute(lastStepReturnValue);
        observers.forEach(observer -> observer.showNextAlgorithmStep(currentStepIndex));
    }

    public void addObserver(LRParsingObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public int getSymbolIndex() {
        return symbolIndex;
    }

    public Deque<Integer> getStateStack() {
        return new ArrayDeque<>(stateStack);
    }

    public Deque<ProductionSymbol> getSymbolStack() {
        return new ArrayDeque<>(symbolStack);
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
            ActionItem actionItem = lrParsingTable.action(state, symbol);
            if (actionItem == null || actionItem.action() != ActionItem.Action.SHIFT) {
                currentStepIndex += 2;
                return null;
            }
            observers.forEach(observer -> observer.addStepData(actionItem));
            return new Object[]{actionItem.number()};
        };
    }

    /**
     * Push t onto stack algorithm step.
     * 将t压入栈中
     *
     * @return the algorithm step
     */
    private AlgorithmStep pushTOntoStack() {
        return parameters -> {
            int t = (Integer) parameters[0];
            stateStack.push(t);
            observers.forEach(observer -> observer.addNodeToState(String.valueOf(t),
                    symbol));
            return null;
        };
    }

    /**
     * 令a为下一个输入符号
     *
     * @return the algorithm step
     */
    private AlgorithmStep letABeTheNextInputSymbol() {
        return parameters -> {
            symbolStack.push(symbol);
            toNextSymbol();
            currentStepIndex = 0;
            return null;
        };
    }

    private AlgorithmStep elseIfReduce() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state, symbol);
            if (actionItem == null || actionItem.action() != ActionItem.Action.REDUCE) {
                currentStepIndex += 4;
                return null;
            }
            observers.forEach(observer -> observer.addStepData(actionItem));
            return new Object[]{actionItem};
        };
    }

    private AlgorithmStep popSymbolFromStack() {
        return parameters -> {
            int expressionIndex = ((ActionItem) parameters[0]).number();
            Production production =
                    Grammars.getExpression(lrParsingTable.getGrammar(), expressionIndex);
            for (int i = 0; i < production.getBody().get(0).length(); ++i) {
                symbolStack.pop();
                stateStack.pop();
            }
            observers.forEach(observer -> observer.rollbackState(production));
            return new Object[]{production};
        };
    }


    /**
     * 令t为当前的栈顶状态
     * <p>在下一个步骤中，可以直接获得栈顶的状态，所以此步无需任何动作</p>
     *
     * @return the algorithm step
     */
    private AlgorithmStep letTBeTheStateOfTheTopOfTheStack() {
        return parameters -> {
            return parameters;
        };
    }

    private AlgorithmStep pushGoOntoStack() {
        return parameters -> {
            if (stateStack.isEmpty()) {
                throw new LRParsingException("stateStack为空");
            }
            Production production = (Production) parameters[0];
            int newState = lrParsingTable.go(stateStack.peek(), production.getHead());
            stateStack.push(newState);
            symbolStack.push(production.getHead());
            observers.forEach(observer ->
                    observer.addNodeToState(String.valueOf(newState), production.getHead()));
            return parameters;
        };
    }

    /**
     * 输出产生式A→β
     *
     * @return the algorithm step
     */
    private AlgorithmStep outputProduction() {
        return parameters -> {
            Production production = (Production) parameters[0];
            observers.forEach(observer -> observer.addParentNodeToTree(production.getHead(),
                    production.getBody().get(0).getValue()));
            currentStepIndex = 0;
            return null;
        };
    }

    private AlgorithmStep elseIfAccept() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state, symbol);
            if (actionItem != null && actionItem.action() == ActionItem.Action.ACCEPT) {
                observers.forEach(observer -> {
                    observer.addStepData(actionItem);
                    observer.completeExecution();
                    observer.addNodeToState("accept", symbol);
                });
                completeExecution();
            }
            return null;
        };
    }

    private AlgorithmStep elseErrorRecovery() {
        return parameters -> {
            ActionItem actionItem = lrParsingTable.action(state, symbol);
            String string = "ACTION[" + stateStack.peek() + "," + symbol.getValue() +
                    "]=" + (actionItem == null ? "NULL" : ActionItem.toString(actionItem));
            observers.forEach(observer ->
                    observer.showException(new LRParsingException(string)));
            completeExecution();
            return null;
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
        observers.forEach(observer -> observer.addNodeToTree(symbol));
        changeSymbol(symbolIndex + 1);
    }

    private void completeExecution() {
        currentStepIndex = algorithmStepList.size();
        observers.forEach(observer -> observer.showNextAlgorithmStep(-1));
    }
}
