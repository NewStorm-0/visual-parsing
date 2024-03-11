package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.grammar.ProductionNotFoundException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
     * Extracting left common factors.提取左公因子
     *
     * @param grammar the grammar
     */
    public static void extractingLeftCommonFactors(Grammar grammar) {
        Set<Nonterminal> formerNonterminals;
        do {
            formerNonterminals = new HashSet<>(grammar.getNonterminals());
            extractingLeftCommonFactorsConverter(grammar);
        } while (!grammar.getNonterminals().equals(formerNonterminals));
    }

    /**
     * Extracting left common factors .提取左公因子
     * <p>改方法被方法extractingLeftCommonFactors重复调用</p>
     *
     * @param grammar the grammar
     */
    private static void extractingLeftCommonFactorsConverter(Grammar grammar) {
        for (Production production : grammar.getProductions().toArray(Production[]::new)) {
            List<ProductionSymbol> longestCommonPrefixList =
                    getLongestCommonPrefix(production);
            if (longestCommonPrefixList.isEmpty()) {
                continue;
            }
            replaceCommonPrefixProduction(grammar, production, longestCommonPrefixList);
        }
    }

    /**
     * Eliminate left recursion grammar.消除文法左递归，但是遍历的集合不会变化，
     * 所以需要被多次调用
     *
     * @param grammar the grammar
     */
    private static void eliminateLeftRecursionRunner(Grammar grammar) {
        Nonterminal[] nonterminals = grammar.getNonterminals().toArray(Nonterminal[]::new);
        // 将文法的开始符号放在第一位
        swapStartSymbolToFirst(nonterminals, grammar.getStartSymbol());

        for (int i = 0; i < nonterminals.length; i++) {
            Grammar backupGrammar = (Grammar) grammar.clone();
            for (int j = 0; j <= i - 1; j++) {
                replaceRecursiveProduction(grammar, nonterminals[i], nonterminals[j]);
            }
            // 消除Ai产生式之间的立即左递归
            Production production = getProductionByHead(nonterminals[i], new ArrayList<>(grammar.getProductions()));
            boolean isLeftRecursionImmediate = eliminateProductionImmediateLeftRecursion(grammar, production);
            // 若没有立即左递归，则将替换的产生式组还原回去
            if (!isLeftRecursionImmediate) {
                grammar.setNonterminals(backupGrammar.getNonterminals());
                grammar.setTerminals(backupGrammar.getTerminals());
                grammar.setProductions(backupGrammar.getProductions());
            }
        }
    }

    /**
     * Swap start symbol to first.将文法的开始符号放在第一位
     *
     * @param nonterminals the nonterminals
     * @param startSymbol  the start symbol
     */
    private static void swapStartSymbolToFirst(Nonterminal[] nonterminals, Nonterminal startSymbol) {
        for (int i = 0; i < nonterminals.length; i++) {
            if (startSymbol.equals(nonterminals[i])) {
                nonterminals[i] = nonterminals[0];
                nonterminals[0] = startSymbol;
                break;
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
    private static void replaceRecursiveProduction(Grammar grammar, Nonterminal ai, Nonterminal aj) {
        Production aiProduction, ajProduction;
        List<Production> productions = new ArrayList<>(grammar.getProductions());
        aiProduction = getProductionByHead(ai, productions);
        ajProduction = getProductionByHead(aj, productions);
        for (Expression aiExpression : aiProduction.getBody().toArray(Expression[]::new)) {
            if (!aiExpression.getValue()[0].equals(aj)) {
                continue;
            }
            generateNewExpression(aiProduction, aiExpression, ajProduction.getBody());
            aiProduction.eraseExpression(aiExpression);
        }
    }

    /**
     * Generate new expressions.生成产生式组Ai→δ1γ|δ2γ|···|δkγ
     *
     * @param aiProduction  the Ai production
     * @param aiExpression  the Ai expression
     * @param ajExpressions the Aj expressions
     */
    private static void generateNewExpression(Production aiProduction, Expression aiExpression, List<Expression> ajExpressions) {
        List<ProductionSymbol> symbolList = new ArrayList<>();
        for (Expression ajexpression : ajExpressions) {
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
    private static boolean eliminateProductionImmediateLeftRecursion(Grammar grammar, Production production) {
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
        grammar.addExpression(auxiliarySymbol, new Expression(new ProductionSymbol[]{Terminal.EMPTY_STRING}));
        return true;
    }

    /**
     * Gets production by head.
     *
     * @param head     the head
     * @param iterable the iterable
     * @return the production by head
     */
    private static Production getProductionByHead(Nonterminal head, Iterable<Production> iterable) {
        for (Production production : iterable) {
            if (production.getHead().equals(head)) {
                return production;
            }
        }
        throw new ProductionNotFoundException("不存在head为" + head.getValue() + "的Production");
    }

    /**
     * Gets longest common prefix.获取最长公共前缀。调用getLongestCommonPrefixRunner方法
     *
     * @param production the production
     * @return the longest common prefix
     */
    private static List<ProductionSymbol> getLongestCommonPrefix(Production production) {
        return getLongestCommonPrefixRunner(production.getBody(), 0, 0);
    }

    /**
     * Gets longest common prefix.获取最长公共前缀，该方法应该由getLongestCommonPrefix调用。
     * <p>要获取到有最多的产生式的最长公共前缀，先找最多产生式有的前缀，然后再一个个符号添加，
     * 看符合条件的产生式会不会减少。若减少，则最长公共前缀不包含该符号。</p>
     *
     * @param expressions   the expressions
     * @param index         the index
     * @param lastMaxNumber 上一次递归得到的有最长公共前缀的表达式的数量
     * @return the longest common prefix
     */
    private static List<ProductionSymbol> getLongestCommonPrefixRunner(Collection<Expression> expressions,
                                                                       int index, int lastMaxNumber) {
        // 有最长公共前缀的表达式的数量
        int maxNumber;
        // 最长公共前缀
        List<ProductionSymbol> longestCommonPrefix = new ArrayList<>();
        // 记录当前每个ProductionSymbol被包含在公共前缀的表达式的数量
        Map<ProductionSymbol, Integer> symbolExpressionNumberMap = new HashMap<>();
        for (Expression expression : expressions) {
            if (index >= expression.getValue().length) {
                continue;
            }
            int number = symbolExpressionNumberMap.getOrDefault(expression.get(index), 0);
            symbolExpressionNumberMap.put(expression.get(index), number + 1);
        }
        if (symbolExpressionNumberMap.isEmpty()) {
            return longestCommonPrefix;
        }
        ProductionSymbol maxSymbol = getMaxNumberSymbolFromMap(symbolExpressionNumberMap);
        maxNumber = symbolExpressionNumberMap.get(maxSymbol);
        // 判断产生式数量会不会减少，若减少，则不需要包含当前符号
        if (lastMaxNumber > maxNumber) {
            return longestCommonPrefix;
        }
        longestCommonPrefix.add(maxSymbol);
        List<Expression> prefixedExpressions = expressions.stream().filter(
                expression -> expression.getValue().length > index
                                && expression.getValue()[index].equals(maxSymbol)
                ).toList();
        longestCommonPrefix.addAll(getLongestCommonPrefixRunner(prefixedExpressions,
                index + 1, maxNumber));
        return longestCommonPrefix;
    }

    /**
     * Gets max number symbol from map.
     *
     * @param map the map
     * @return the max number symbol from map
     */
    private static ProductionSymbol getMaxNumberSymbolFromMap(Map<ProductionSymbol, Integer> map) {
        ProductionSymbol symbol = null;
        int maxNumber = 0;
        for (Map.Entry<ProductionSymbol, Integer> entry : map.entrySet()) {
            if (symbol == null) {
                symbol = entry.getKey();
                maxNumber = entry.getValue();
                continue;
            }
            // 即使有两个相同的最大值也没事，任期其一即可
            if (entry.getValue() > maxNumber) {
                symbol = entry.getKey();
                maxNumber = entry.getValue();
            }
        }
        return symbol;
    }

    /**
     * Replace common prefix production.
     * <p>将所有A产生式A→αβ1|αβ2|···|αβn|γ替换为A→αA'|γ，A'→β1|β2|···|βn</p>
     *
     * @param grammar    the grammar
     * @param production the production
     * @param prefixList the prefix symbol list
     */
    private static void replaceCommonPrefixProduction(Grammar grammar,
                                                      Production production,
                                                      List<ProductionSymbol> prefixList) {
        List<Expression> prefixedExpressionList = new ArrayList<>();
        for (Expression expression : production.getBody()) {
            ProductionSymbol[] expressionSymbols = expression.getValue();
            if (isArrayStartsWithList(expressionSymbols, prefixList)) {
                prefixedExpressionList.add(expression);
            }
        }
        // 如果只有一个表达式含有该前缀，则无需替换
        if (prefixedExpressionList.size() == 1) {
            return;
        }
        Nonterminal auxiliarySymbol = getAuxiliaryNonterminal(grammar,
                production.getHead());
        grammar.addNonterminal(auxiliarySymbol);
        // 添加 A→αA'
        List<ProductionSymbol> expressionValue = new ArrayList<>(prefixList);
        expressionValue.add(auxiliarySymbol);
        production.addExpression(new Expression(expressionValue));
        // 删除 A→αβ1|αβ2|···|αβn，并添加A'→β1|β2|···|βn
        for (Expression expression : prefixedExpressionList) {
            expressionValue.clear();
            expressionValue.addAll(expression.getValueList());
            expressionValue = expressionValue.subList(prefixList.size(),
                    expressionValue.size());
            if (expressionValue.isEmpty()) {
                expressionValue.add(Terminal.EMPTY_STRING);
            }
            grammar.addExpression(auxiliarySymbol, new Expression(expressionValue));
            production.eraseExpression(expression);
        }

    }

    /**
     * Is array starts with list boolean.
     * <p>若数组以symbolList中元素开头，则返回true。否则返回false。</p>
     *
     * @param symbols    ProductionSymbol数组
     * @param symbolList ProductionSymbol List
     * @return the boolean
     */
    private static boolean isArrayStartsWithList(ProductionSymbol[] symbols,
                                            List<ProductionSymbol> symbolList) {
        if (symbols.length < symbolList.size()) {
            return false;
        }
        for (int i = 0; i < symbolList.size(); i++) {
            if (!symbols[i].equals(symbolList.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets auxiliary nonterminal.获取一个非终结符号的辅助符号
     * <p>在原先符号value的基础上不断加'，直到grammar不包含该符号</p>
     *
     * @param grammar       the grammar
     * @param initialSymbol the initial symbol
     * @return the auxiliary nonterminal
     */
    private static Nonterminal getAuxiliaryNonterminal(Grammar grammar,
                                                       Nonterminal initialSymbol) {
        String symbolValue = initialSymbol.getValue() + "'";
        try {
            while (true) {
                grammar.getProductionSymbol(symbolValue);
                symbolValue = symbolValue + "'";
            }
        } catch (UnknownSymbolException e) {
            return new Nonterminal(symbolValue);
        }
    }

}
