package com.chaldea.visualparsing.debug;

import com.chaldea.visualparsing.exception.NullPredictivParsingCellException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.ProductionSymbol;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.PredictiveParsingTable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 预测分析算法
 */
public class PredictiveAnalyticsAlgorithm extends StepwiseAlgorithm {

    private final PredictiveParsingTable predictiveTable;
    private final Nonterminal startSymbol;
    private final List<Terminal> inputSymbols;
    private final Deque<ProductionSymbol> stack;

    private final List<PredictiveAnalyticsObserver> observers;

    /**
     * 当前正在处理的符号的索引
     */
    private int ip;

    private ProductionSymbol X;

    public PredictiveAnalyticsAlgorithm(PredictiveParsingTable table,
                                        Nonterminal startSymbol,
                                        List<Terminal> input) {
        predictiveTable = table;
        this.startSymbol = startSymbol;
        this.inputSymbols = new ArrayList<>(input);
        this.inputSymbols.add(Terminal.END_MARKER);
        observers = new ArrayList<>();
        algorithmStepList = new ArrayList<>();
        stack = new LinkedList<>();
        algorithmStepList.add(judgeXNotEqualsEndMarker());
        algorithmStepList.add(ifXEqualsIpSymbol());
        algorithmStepList.add(elseIfXIsTerminal());
        algorithmStepList.add(elseIfMIsAWrongItem());
        algorithmStepList.add(finalElse());
        algorithmStepList.add(letXEqualsTopOfTheStack());
    }

    public int getIp() {
        return ip;
    }

    public Deque<ProductionSymbol> getStack() {
        return new LinkedList<>(stack);
    }

    public void addObserver(PredictiveAnalyticsObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void reset() {
        currentStepIndex = 0;
        lastStepReturnValue = null;
        ip = 0;
        stack.clear();
        stack.push(Terminal.END_MARKER);
        stack.push(startSymbol);
        X = stack.peek();
        observers.forEach(observer -> observer.showNextAlgorithmStep(0));
    }

    @Override
    public void executeStep() {
        lastStepReturnValue =
                algorithmStepList.get(currentStepIndex++).execute(lastStepReturnValue);
        observers.forEach(observer -> observer.showNextAlgorithmStep(currentStepIndex));
    }

    private AlgorithmStep judgeXNotEqualsEndMarker() {
        return parameters -> {
            if (X.equals(Terminal.END_MARKER)) {
                completeExecution();
                observers.forEach(observer ->
                        observer.addStepData(PredictiveAnalyticsObserver.ACCEPT, null,
                                null));
                observers.forEach(PredictiveAnalyticsObserver::completeExecution);
            }
            return null;
        };
    }

    private AlgorithmStep ifXEqualsIpSymbol() {
        return parameters -> {
            if (X.equals(inputSymbols.get(ip))) {
                observers.forEach(observer ->
                        observer.addStepData(PredictiveAnalyticsObserver.MATCH,
                                null, null));
                stack.pop();
                ip += 1;
                currentStepIndex = algorithmStepList.size() - 1;
            }
            return null;
        };
    }

    private AlgorithmStep elseIfXIsTerminal() {
        return parameters -> {
            if (X instanceof Terminal) {
                abortExecution();
                observers.forEach(observer ->
                        observer.showException(new UnknownSymbolException(X.getValue())));
            }
            return null;
        };
    }

    private AlgorithmStep elseIfMIsAWrongItem() {
        return parameters -> {
            try {
                predictiveTable.get((Nonterminal) X, inputSymbols.get(ip));
            } catch (NullPointerException e) {
                abortExecution();
                observers.forEach(observer ->
                        observer.showException(new NullPredictivParsingCellException(
                                (Nonterminal) X,
                                inputSymbols.get(ip)
                        )));
            }
            return null;
        };
    }

    private AlgorithmStep finalElse() {
        return parameters -> {
            Expression expression = predictiveTable.get((Nonterminal) X,
                    inputSymbols.get(ip));
            observers.forEach(observer ->
                    observer.addStepData(PredictiveAnalyticsObserver.REPLACE,
                            (Nonterminal) X, expression));
            stack.pop();
            ProductionSymbol[] body = expression.getValue();
            for (int i = body.length - 1; i >= 0; i--) {
                if (Terminal.EMPTY_STRING.equals(body[i])) {
                    continue;
                }
                stack.push(body[i]);
            }
            return null;
        };
    }

    private AlgorithmStep letXEqualsTopOfTheStack() {
        return parameters -> {
            X = stack.peek();
            currentStepIndex = 0;
            return null;
        };
    }

    /**
     * 执行出现错误，中止执行.
     * 结束当前执行，将currentStepIndex设置为超出最大值
     */
    private void abortExecution() {
        // -1 代表执行异常
        currentStepIndex = -1;
        observers.forEach(observer -> observer.showNextAlgorithmStep(-1));
    }

    /**
     * Complete execution.正常执行完毕
     */
    private void completeExecution() {
        currentStepIndex = algorithmStepList.size();
        observers.forEach(observer -> observer.showNextAlgorithmStep(-1));
    }

}
