package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.grammar.*;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * LL(1)文法自顶向下预测分析法的预测分析表
 */
public class PredictiveParsingTable {
    private final Grammar grammar;
    private final Expression[][] table;

    /**
     * 记录表中行对应的索引号，非终结符转换为行号
     */
    private final Map<Nonterminal, Integer> nonterminalMap;

    /**
     * 记录表中列对应的索引号，输入的终结符及结束标记转为列号
     */
    private final Map<Terminal, Integer> inputSymbolMap;

    public PredictiveParsingTable(Grammar grammar) {
        if (grammar == null || grammar.isEmpty()) {
            throw new BaseException("grammar 为 null 或是 empty");
        }
        this.grammar = grammar;
        table = new Expression[grammar.getNonterminals().size()][grammar.getTerminals().size() + 1];
        nonterminalMap = new HashMap<>(grammar.getNonterminals().size());
        inputSymbolMap = new HashMap<>(grammar.getTerminals().size());
        // 初始化行列号映射
        int index = 0;
        for (Nonterminal nonterminal : grammar.getNonterminals()) {
            nonterminalMap.put(nonterminal, index++);
        }
        index = 0;
        for (Terminal terminal : grammar.getTerminals()) {
            inputSymbolMap.put(terminal, index++);
        }
        inputSymbolMap.put(Terminal.END_MARKER, index);
    }

    /**
     * Set.
     *
     * @param nonterminal the nonterminal
     * @param inputSymbol the input symbol
     * @param expression  the expression
     */
    public void set(Nonterminal nonterminal, Terminal inputSymbol, Expression expression) {
        // 行号
        int rowNumber = nonterminalMap.get(nonterminal);
        // 列号
        int colNumber = inputSymbolMap.get(inputSymbol);
        table[rowNumber][colNumber] = expression.copy();
    }

    /**
     * Set.
     *
     * @param nonterminalValue the nonterminal value
     * @param inputSymbolValue the input symbol value
     * @param expression       the expression
     */
    public void set(String nonterminalValue, String inputSymbolValue, Expression expression) {
        Nonterminal nonterminal = grammar.getNonterminal(nonterminalValue);
        Terminal inputSymbol = grammar.getTerminal(inputSymbolValue);
        set(nonterminal, inputSymbol, expression);
    }

    /**
     * Get pair.
     *
     * @param nonterminal the nonterminal
     * @param inputSymbol the input symbol
     * @return the expression
     */
    public Expression get(Nonterminal nonterminal,
                                             Terminal inputSymbol) {
        // 行号
        int rowNumber = nonterminalMap.get(nonterminal);
        // 列号
        int colNumber = inputSymbolMap.get(inputSymbol);
        return table[rowNumber][colNumber].copy();
    }

    public Expression get(String nonterminalValue,
                                             String inputSymbolValue) {
        return get(grammar.getNonterminal(nonterminalValue),
                grammar.getTerminal(inputSymbolValue));
    }

    public Expression[][] getTable() {
        return table.clone();
    }

    public Map<Nonterminal, Integer> getNonterminalMap() {
        return new HashMap<>(nonterminalMap);
    }

    public Map<Terminal, Integer> getInputSymbolMap() {
        return new HashMap<>(inputSymbolMap);
    }

    /**
     * To formatted table string string.
     *
     * @return the string
     */
    public String toFormattedTableString() {
        StringBuilder stringBuilder = new StringBuilder(1024);
        for (Expression[] row : table) {
            for (Expression e : row) {
                if (e == null) {
                    stringBuilder.append(String.format("%-12s", "null")).append('\t');
                    continue;
                }
//                StringBuilder temp = new StringBuilder(16);
//                for (ProductionSymbol symbol : e.getValue()) {
//                    temp.append(symbol.getValue()).append(", ");
//                }
//                temp.delete(temp.length() - 2, temp.length() - 1);
                stringBuilder.append(String.format("%-12s",
                        Arrays.toString(e.getValue()))).append('\t');
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "PredictiveParsingTable{" +
                "table=" + Arrays.toString(table) +
                ", nonterminalMap=" + nonterminalMap +
                ", inputSymbolMap=" + inputSymbolMap +
                '}';
    }
}
