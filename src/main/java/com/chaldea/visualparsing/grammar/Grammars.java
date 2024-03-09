package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.grammar.ProductionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Grammars {

    private static final Logger logger = LoggerFactory.getLogger(Grammars.class);
    private Grammars() {
    }

    /**
     * Eliminate left recursion.消除文法左递归
     *
     * @param grammar the grammar
     */
    public static void eliminateLeftRecursion(Grammar grammar) {
        Grammar grammarCopy;
        do {
            grammarCopy = (Grammar) grammar.clone();
            eliminateLeftRecursionRunner(grammar);
        } while (!grammarCopy.equals(grammar));
    }

    /**
     * Eliminate left recursion grammar.消除文法左递归，但是遍历的集合不会变化，
     * 所以需要被多次调用
     *
     * @param grammar the grammar
     */
    private static void eliminateLeftRecursionRunner(Grammar grammar) {
        Nonterminal[] nonterminals =
                grammar.getNonterminals().toArray(Nonterminal[]::new);
        for (int i = 0; i < nonterminals.length; i++) {
            Grammar backupGrammar = (Grammar) grammar.clone();
            for (int j = 0; j <= i - 1; j++) {
                replaceProduction(grammar, nonterminals[i], nonterminals[j]);
            }
            // 消除Ai产生式之间的立即左递归
            boolean isLeftRecursionImmediate = false;
            for (Production production : grammar.getProductions().toArray(Production[]::new
            )) {
                if (production.getHead().equals(nonterminals[i])) {
                    isLeftRecursionImmediate = eliminateImmediateLeftRecursion(grammar,
                            production) || isLeftRecursionImmediate;
                }
            }
            // 若没有立即左递归，则将替换的产生式组还原回去
            if (!isLeftRecursionImmediate) {
                grammar.setNonterminals(backupGrammar.getNonterminals());
                grammar.setTerminals(backupGrammar.getTerminals());
                grammar.setProductions(backupGrammar.getProductions());
            }
        }
    }

    /**
     * Replace production.
     * <p>将每个形如Ai→Ajγ的产生式替换为产生式组Ai→δ1γ|δ2γ|···|δkγ，
     * 其中Aj→δ1|δ2|···|δk是所有的Aj产生式</p>
     *
     * @param grammar the grammar
     * @param ai      Ai
     * @param aj      Aj
     */
    private static void replaceProduction(Grammar grammar, Nonterminal ai,
                                          Nonterminal aj) {
        Production aiProduction = null, ajProduction = null;
        for (Production p : grammar.getProductions().toArray(Production[]::new)) {
            if (p.getHead().equals(ai)) {
                aiProduction = p;
            }
            if (p.getHead().equals(aj)) {
                ajProduction = p;
            }
            if (aiProduction != null && ajProduction != null) {
                break;
            }
        }
        if (aiProduction == null || ajProduction == null) {
            throw new ProductionNotFoundException();
        }
        for (Expression aiExpression : aiProduction.getBody().toArray(Expression[]::new)) {
            if (!aiExpression.getValue()[0].equals(aj)) {
                continue;
            }
            List<ProductionSymbol> symbolList = new ArrayList<>();
            for (Expression ajexpression : ajProduction.getBody()) {
                // 检测δ是否为ε
                if (!Terminal.EMPTY_STRING.equals(ajexpression.getValue()[0])) {
                    symbolList.addAll(ajexpression.getValueList());
                }
                List<ProductionSymbol> tempList = aiExpression.getValueList();
                symbolList.addAll(tempList.subList(1, tempList.size()));
                // 检测δ为ε且γ为空，即γ长度为0
                if (symbolList.isEmpty()) {
                    continue;
                }
                aiProduction.addExpression(new Expression(symbolList));
                symbolList.clear();
            }
            aiProduction.eraseExpression(aiExpression);
        }
    }

    /**
     * Eliminate immediate left recursion.
     * 消除立即左递归
     * <p>将A→Aα|β替换为：A→βA', A'→αA'|ε</p>
     *
     * @param grammar    the grammar
     * @param production the production
     * @return 若有立即左递归，则返回true；否则返回false
     */
    private static boolean eliminateImmediateLeftRecursion(Grammar grammar,
                                                     Production production) {
        List<Expression> recursionExpressionList = new ArrayList<>();
        List<Expression> normalExpressionList = new ArrayList<>();
        for (Expression expression : production.getBody()) {
            List<ProductionSymbol> valueList = expression.getValueList();
            // 处理 A→A  该段代码貌似判定不会成立，因为在replaceProduction就进行了避免该情况的处理
            if (valueList.size() == 1 && valueList.get(0).equals(production.getHead())) {
                grammar.deleteExpression(production.getHead(), expression);
                continue;
            }
            if (valueList.get(0).equals(production.getHead())) {
                recursionExpressionList.add(expression);
            } else {
                normalExpressionList.add(expression);
            }
        }
        // 若该产生式没有立即左递归
        if (recursionExpressionList.isEmpty()) {
            return false;
        }
        production.getBody().clear();
        // 为辅助符号取名
        Nonterminal auxiliarySymbol = new Nonterminal(production.getHead().getValue() + "'");
        while (grammar.getNonterminals().contains(auxiliarySymbol)) {
            auxiliarySymbol = new Nonterminal(auxiliarySymbol.getValue() + "'");
        }
        grammar.addNonterminal(auxiliarySymbol);
        // 添加 A→βA'
        for (Expression expression : normalExpressionList) {
            // 处理 β 为 ε
            if (Terminal.EMPTY_STRING.equals(expression.getValue()[0])) {
                production.addExpression(new Expression(new ProductionSymbol[]{auxiliarySymbol}));
                continue;
            }
            List<ProductionSymbol> symbolList = new ArrayList<>(expression.getValueList());
            symbolList.add(auxiliarySymbol);
            production.addExpression(new Expression(symbolList));
        }
        // 添加 A'→αA'
        for (Expression expression : recursionExpressionList) {
            List<ProductionSymbol> tempList = expression.getValueList();
            tempList = tempList.subList(1, tempList.size());
            List<ProductionSymbol> symbolList = new ArrayList<>(tempList);
            symbolList.add(auxiliarySymbol);
            grammar.addExpression(auxiliarySymbol, new Expression(symbolList));
        }
        // 添加 A'→ε
        grammar.addExpression(auxiliarySymbol,
                new Expression(new ProductionSymbol[]{Terminal.EMPTY_STRING}));
        return true;
    }

}
