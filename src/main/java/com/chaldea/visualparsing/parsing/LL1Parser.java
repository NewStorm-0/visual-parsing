package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.grammar.IllegalSymbolException;
import com.chaldea.visualparsing.exception.grammar.ProductionNotFoundException;
import com.chaldea.visualparsing.grammar.*;

import java.util.*;

/**
 * LL(1)语法分析器
 */
public class LL1Parser {
    private Grammar grammar;

    private Map<ProductionSymbol, Set<Terminal>> symbolFirstMap;

    private Map<Nonterminal, Set<Terminal>> symbolFollowMap;

    public LL1Parser() {
    }

    /**
     * Instantiates a new Ll 1 parser.
     *
     * @param grammar the grammar
     */
    public LL1Parser(Grammar grammar) {
        this.grammar = grammar;
        symbolFirstMap =
                new HashMap<>(grammar.getTerminals().size()
                        + grammar.getNonterminals().size());
        symbolFollowMap = new HashMap<>(grammar.getNonterminals().size());
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    /**
     * 获取文法符号symbol的FIRST(symbol)
     * <p>返回的值是一份拷贝，对其修改不会影响到symbolFirstMap</p>
     * @param symbol the symbol
     * @return the set
     */
    public Set<Terminal> first(ProductionSymbol symbol) {
        if (symbol == null) {
            throw new IllegalSymbolException("文法终结符号为null");
        }
        Set<Terminal> firstSet = symbolFirstMap.get(symbol);
        if (firstSet != null) {
            return new HashSet<>(firstSet);
        }
        firstSet = new HashSet<>();
        symbolFirstMap.put(symbol, firstSet);
        if (symbol instanceof Terminal) {
            firstSet.add((Terminal) symbol);
        } else if (symbol instanceof Nonterminal) {
            // 获取到左部为symbol的产生式
            List<Production> matchedProductionList = grammar.getProductions().stream()
                    .filter(e -> e.getHead().equals(symbol)).toList();
            if (matchedProductionList.isEmpty()) {
                throw new ProductionNotFoundException();
            }
            Production production = matchedProductionList.get(0);
            for (Expression expression : production.getBody()) {
                firstSet.addAll(first(expression));
            }
        } else {
            throw new IllegalSymbolException("未知的文法符号类型");
        }
        return new HashSet<>(firstSet);
    }

    /**
     * 根据表达式计算first集合
     * <p>X→Y1Y2Y3</p>
     *
     * @param expression the expression
     * @return the set
     */
    public Set<Terminal> first(Expression expression) {
        Set<Terminal> firstSet = new HashSet<>();
        // symbol→ε 是一个产生式，即表达式体中只有一个ε
        if (expression.isEmptyString()) {
            firstSet.add(Terminal.EMPTY_STRING);
            return firstSet;
        }
        int index = 0;
        Set<Terminal> symbolFirstSet;
        boolean containEmptyString;
        do {
            symbolFirstSet = first(expression.get(index));
            containEmptyString = symbolFirstSet.remove(Terminal.EMPTY_STRING);
            firstSet.addAll(symbolFirstSet);
            ++index;
        } while (containEmptyString && index < expression.getValue().length);
        // 当所有Y→ε时
        if (index == expression.getValue().length
                && first(expression.get(index - 1)).contains(Terminal.EMPTY_STRING)) {
            firstSet.add(Terminal.EMPTY_STRING);
        }
        return firstSet;
    }

    /**
     * Follow set.
     * <p>返回的值是一份拷贝，对其修改不会影响到symbolFollowMap</p>
     * @param symbol the symbol
     * @return the set
     */
    public Set<Terminal> follow(Nonterminal symbol) {
        if (symbol == null) {
            throw new IllegalSymbolException("文法非终结符号为null");
        }
        Set<Terminal> followSet = symbolFollowMap.get(symbol);
        if (followSet != null) {
            return followSet;
        }
        followSet = new HashSet<>();
        symbolFollowMap.put(symbol, followSet);
        // 若 symbol 是开始符号，则将 # 放入到 FOLLOW(symbol) 中
        if (grammar.getStartSymbol().equals(symbol)) {
            followSet.add(Terminal.END_MARKER);
        }
        for(Production production : grammar.getProductions()) {
            followSet.addAll(follow(symbol, production));
        }
        return new HashSet<>(followSet);
    }

    /**
     * Follow set.
     *
     * @param symbol     the symbol
     * @param production the production
     * @return the set
     */
    private Set<Terminal> follow(Nonterminal symbol, Production production) {
        Set<Terminal> followSet = new HashSet<>();
        for (Expression expression : production.getBody()) {
            List<ProductionSymbol> expressionValue = List.of(expression.getValue());
            if (!expressionValue.contains(symbol)) {
                continue;
            }
            ProductionSymbol[] productionSymbols = expressionValue.subList(
                    expressionValue.indexOf(symbol) + 1,
                    expressionValue.size()).toArray(ProductionSymbol[]::new);
            // 若 B→α<symbol> 或B→α<symbol>β是一个产生式且有ε∈FIRST(β)
            if (expressionValue.get(expressionValue.size() - 1).equals(symbol)
                || first(new Expression(productionSymbols)).contains(Terminal.EMPTY_STRING)) {
                followSet.addAll(follow(production.getHead()));
            }
            if (productionSymbols.length != 0) {
                // 若 B→α<symbol>β
                Set<Terminal> firstSet = first(new Expression(productionSymbols));
                firstSet.remove(Terminal.EMPTY_STRING);
                followSet.addAll(firstSet);
            }
        }
        return followSet;
    }
}
