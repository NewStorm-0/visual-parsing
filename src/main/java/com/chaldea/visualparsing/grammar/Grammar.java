package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.grammar.EmptyHeadProductionException;
import com.chaldea.visualparsing.exception.grammar.IllegalSymbolException;
import com.chaldea.visualparsing.exception.grammar.RepeatedSymbolException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;

import java.io.Serializable;
import java.util.*;

/**
 * 文法
 */
public class Grammar implements Serializable, Cloneable {
    /**
     * 产生式集合
     * <p>当产生式的数量很大时，可以将 {@code List<Production>} 改为
     * {@code Map<Nonterminal, Production>} 来提高性能</p>
     */
    private List<Production> productions;
    /**
     * 非终结符集合
     */
    private Set<Nonterminal> nonterminals;
    /**
     * 终结符集合
     */
    private Set<Terminal> terminals;
    /**
     * 开始符号
     */
    private Nonterminal startSymbol;

    /**
     * Instantiates a new Grammar.
     *
     * @param start 开始符号
     */
    public Grammar(String start) {
        this(new Nonterminal(start));
    }

    /**
     * Instantiates a new Grammar.
     *
     * @param start the start
     */
    public Grammar(Nonterminal start) {
        productions = new ArrayList<>();
        nonterminals = new HashSet<>();
        terminals = new HashSet<>();
        nonterminals.add(start);
        startSymbol = start;
    }

    /**
     * Gets productions.
     *
     * @return the productions
     */
    public List<Production> getProductions() {
        return productions;
    }

    /**
     * Sets productions.
     *
     * @param productions the productions
     */
    public void setProductions(List<Production> productions) {
        this.productions = productions;
    }

    /**
     * Gets nonterminals.
     *
     * @return the nonterminals
     */
    public Set<Nonterminal> getNonterminals() {
        return nonterminals;
    }

    /**
     * Sets nonterminals.
     *
     * @param nonterminals the nonterminals
     */
    public void setNonterminals(Set<Nonterminal> nonterminals) {
        this.nonterminals = nonterminals;
    }

    /**
     * Gets terminals.
     *
     * @return the terminals
     */
    public Set<Terminal> getTerminals() {
        return terminals;
    }

    /**
     * Sets terminals.
     *
     * @param terminals the terminals
     */
    public void setTerminals(Set<Terminal> terminals) {

        this.terminals = terminals;
    }

    /**
     * Gets start symbol.
     *
     * @return the start symbol
     */
    public Nonterminal getStartSymbol() {
        return startSymbol;
    }

    /**
     * Gets production symbol.获取ProductionSymbol对象
     *
     * @param value the symbol value
     * @return the production symbol
     * @throws UnknownSymbolException 不包含该value的文法符号
     */
    public ProductionSymbol getProductionSymbol(String value) {
        for (Nonterminal nonterminal : nonterminals) {
            if (nonterminal.getValue().equals(value)) {
                return nonterminal;
            }
        }
        for (Terminal terminal : terminals) {
            if (terminal.getValue().equals(value)) {
                return terminal;
            }
        }
        throw new UnknownSymbolException("不包含value为" + value + "的文法符号");
    }

    /**
     * Generate expression.
     * <p>每个symbol的value是一个String对象</p>
     *
     * @param values the values
     * @return the expression
     */
    public Expression generateExpression(String... values) {
        List<ProductionSymbol> productionSymbols = new ArrayList<>(values.length);
        for (String value : values) {
            productionSymbols.add(getProductionSymbol(value));
        }
        return new Expression(productionSymbols.toArray(ProductionSymbol[]::new));
    }

    /**
     * Convert string to expression.
     * <p>对字符串进行解析，被<>括住的是一个文法符号，不被括住的，每一个字符视为一个文法符号</p>
     *
     * @param values the values
     * @return the expression
     */
    public Expression convertStringToExpression(String values) {
        List<String> stringList = new ArrayList<>();
        for (int index = 0; index < values.length();) {
            index = values.indexOf('<');
            if (index == -1) {
                break;
            }
            String singleSymbolString = values.substring(0, index);
            String[] strings = new String[singleSymbolString.length()];
            for (int i = 0; i < singleSymbolString.length(); i++) {
                strings[i] = String.valueOf(singleSymbolString.charAt(i));
            }
            stringList.addAll(List.of(strings));
            // 将 values 从上一次找到 < 的地方裁剪
            values = values.substring(index + 1);
            index = values.indexOf('>');
            if (index == -1) {
                throw new IllegalSymbolException("单独出现的 <");
            }
            stringList.add(values.substring(0, index));
            // 将 values 从找到 > 的地方后一位开始裁剪
            values = values.substring(index + 1);
            index = 0;
        }
        if (!values.isEmpty()) {
            String[] strings = new String[values.length()];
            for (int i = 0; i < values.length(); i++) {
                strings[i] = String.valueOf(values.charAt(i));
            }
            stringList.addAll(List.of(strings));
        }
        return generateExpression(stringList.toArray(String[]::new));
    }

    /**
     * Gets nonterminal.
     *
     * @param value the value
     * @return the nonterminal
     */
    public Nonterminal getNonterminal(String value) {
        for (Nonterminal nonterminal : nonterminals) {
            if (nonterminal.getValue().equals(value)) {
                return nonterminal;
            }
        }
        throw new UnknownSymbolException();
    }

    /**
     * Gets terminal.
     *
     * @param value the value
     * @return the terminal
     */
    public Terminal getTerminal(String value) {
        for (Terminal terminal : terminals) {
            if (terminal.getValue().equals(value)) {
                return terminal;
            }
        }
        throw new UnknownSymbolException();
    }

    /**
     * Sets start symbol.
     *
     * @param startSymbol the start symbol
     */
    public void setStartSymbol(Nonterminal startSymbol) {
        if (!nonterminals.contains(startSymbol)) {
            throw new IllegalSymbolException("开始符号必须为一个非终结符");
        }
        this.startSymbol = startSymbol;
    }

    /**
     * Add terminal.
     *
     * @param symbol the symbol
     * @throws RepeatedSymbolException 抛出异常
     * @throws IllegalSymbolException 添加代表空串的符号时出现的异常
     */
    public void addTerminal(Terminal symbol) {
        if (terminals.contains(symbol)) {
            throw new RepeatedSymbolException("已经包含该终结符");
        }
        if (nonterminals.contains(new Nonterminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("非终结符集合中已经包含该符号");
        }
        if (Terminal.EMPTY_STRING.equals(symbol)) {
            throw new IllegalSymbolException("不可添加空串" + Terminal.EMPTY_STRING.getValue());
        }
        if (Terminal.END_MARKER.equals(symbol)) {
            throw new IllegalSymbolException("不可添加结束标记" + Terminal.END_MARKER.getValue());
        }
        terminals.add(symbol);
    }

    /**
     * Add terminal.
     *
     * @param symbol the symbol
     */
    public void addTerminal(String symbol) {
        addTerminal(new Terminal(symbol));
    }

    /**
     * Remove terminal boolean.
     *
     * @param symbol the symbol
     * @return the boolean
     */
    public boolean removeTerminal(Terminal symbol) {
        return terminals.remove(symbol);
    }

    /**
     * Add nonterminal.
     *
     * @param symbol the symbol
     * @throws RepeatedSymbolException 抛出异常
     * @throws IllegalSymbolException 符号值不符合要求
     */
    public void addNonterminal(Nonterminal symbol) {
        if (terminals.contains(new Terminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("终结符集合已经包含该符号");
        }
        if (nonterminals.contains(new Nonterminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("已经包含该非终结符");
        }
        if (Terminal.EMPTY_STRING.getValue().equals(symbol.getValue())) {
            throw new IllegalSymbolException("值不能同空串符号" + Terminal.EMPTY_STRING.getValue() + "相同");
        }
        if (Terminal.END_MARKER.getValue().equals(symbol.getValue())) {
            throw new IllegalSymbolException("值不能同结束标记" + Terminal.END_MARKER.getValue() + "相同");
        }
        nonterminals.add(symbol);
    }

    /**
     * Add nonterminal
     * @param symbol the symbol
     */
    public void addNonterminal(String symbol) {
        addNonterminal(new Nonterminal(symbol));
    }

    /**
     * Remove nonterminal boolean.
     *
     * @param symbol the symbol
     * @return the boolean
     */
    public boolean removeNonterminal(Nonterminal symbol) {
        return nonterminals.remove(symbol);
    }

    /**
     * 返回 {@code true} 如果 productions 没有任何元素
     *
     * @return {@code true} if productions contains no elements
     */
    public boolean isEmpty() {
        return productions.isEmpty();
    }

    /**
     * 向文法中添加一个规则
     * <p>若没有相应头的产生式({@link com.chaldea.visualparsing.grammar.Production})，
     * 则先添加一个产生式。若有相应头的产生式，则向该产生式添加一个表达式
     * ({@link com.chaldea.visualparsing.grammar.Expression})</p>
     *
     * @param head the head
     * @param exp  the exp
     * @throws EmptyHeadProductionException 产生式头不能为空
     * @throws UnknownSymbolException 产生式头部不在非终结符定义中
     * @throws IllegalSymbolException 表达式中含有非法符号
     */
    public void addExpression(Nonterminal head, Expression exp) {
        if (head == null) {
            throw new EmptyHeadProductionException();
        }
        if (!nonterminals.contains(head)) {
            throw new UnknownSymbolException();
        }
        if (!checkExpressionSymbols(exp)) {
            throw new IllegalSymbolException("表达式中含有非法符号");
        }
        for (Production production : productions) {
            if (production.getHead().equals(head)) {
                production.addExpression(exp);
                return;
            }
        }
        Production p = new Production(head);
        p.addExpression(exp);
        productions.add(p);
    }

    /**
     * Add expression.
     *
     * @param headValue the head value
     * @param exp       the exp
     */
    public void addExpression(String headValue, Expression exp) {
        Nonterminal head = getNonterminal(headValue);
        addExpression(head, exp);
    }

    /**
     * 从文法中删除一个规则
     * <p>若包含该规则的产生式只有这一个表达式，则将该产生式直接删除。
     * 否则，删除相应产生式中的该表达式</p>
     *
     * @param head the head
     * @param exp  the exp
     */
    public void deleteExpression(Nonterminal head, Expression exp) {
        for (Production p : productions) {
            if (p.getHead().equals(head)) {
                p.eraseExpression(exp);
                if (p.getBody().isEmpty()) {
                    productions.remove(p);
                }
                break;
            }
        }
    }

    /**
     * 修改文法中的一条规则
     *
     * @param oldHead       the old head
     * @param oldExpression the old expression
     * @param newHead       the new head
     * @param newExpression the new expression
     */
    public void modifyExpression(Nonterminal oldHead, Expression oldExpression,
                                 Nonterminal newHead, Expression newExpression) {
        if (newHead == null) {
            throw new EmptyHeadProductionException();
        }
        if (!nonterminals.contains(newHead)) {
            throw new UnknownSymbolException();
        }
        if (!checkExpressionSymbols(newExpression)) {
            throw new IllegalSymbolException("表达式中含有非法符号");
        }
        // 若修改前后，产生式头部不变，则替换相应产生式的表达式
        if (oldHead.equals(newHead)) {
            for (Production production : productions) {
                if (production.getHead().equals(newHead)) {
                    production.modifyExpression(oldExpression, newExpression);
                }
            }
        } else {
            // 若产生式头部变化，先去除对应产生式中表达式，再增添新产生式的表达式
            deleteExpression(oldHead, oldExpression);
            addExpression(newHead, newExpression);
        }
    }

    @Override
    public String toString() {
        return "Grammar{" +
                "productions=" + productions +
                ", nonterminals=" + nonterminals +
                ", terminals=" + terminals +
                ", startSymbol=" + startSymbol +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Grammar grammar = (Grammar) o;
        return Objects.equals(getProductions(), grammar.getProductions()) && Objects.equals(getNonterminals(), grammar.getNonterminals()) && Objects.equals(getTerminals(), grammar.getTerminals()) && Objects.equals(getStartSymbol(), grammar.getStartSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductions(), getNonterminals(), getTerminals(), getStartSymbol());
    }

    @Override
    public Object clone() {
        try {
            Grammar clone = (Grammar) super.clone();
            clone.terminals = new HashSet<>(this.terminals);
            clone.nonterminals = new HashSet<>(this.nonterminals);
            clone.startSymbol = this.startSymbol;
            clone.productions = new ArrayList<>();
            for (Production production : this.productions) {
                Production cloneProduction = new Production(production.getHead());
                for (Expression expression : production.getBody()) {
                    cloneProduction.addExpression(expression.copy());
                }
                clone.productions.add(cloneProduction);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProductionsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Production production : productions) {
            stringBuilder.append(production.getHead().getValue()).append("→");
            for (Expression expression : production.getBody()) {
                for (ProductionSymbol symbol : expression.getValue()) {
                    stringBuilder.append(symbol.getValue()).append(" ");
                }
                stringBuilder.append("| ");
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 检查表达式中符号是否已经被包含在相应的符号集合中
     * @param expression 表达式
     * @return 如果都包含则返回 true，否则返回 false
     */
    private boolean checkExpressionSymbols(Expression expression) {
        for (ProductionSymbol symbol : expression.getValue()) {
            if (symbol.equals(Terminal.EMPTY_STRING)) {
                return true;
            }
            if (symbol.equals(Terminal.END_MARKER)) {
                return false;
            }
            if (symbol instanceof Terminal && !terminals.contains((Terminal) symbol)) {
                return false;
            } else if (symbol instanceof Nonterminal
                    && !nonterminals.contains((Nonterminal) symbol)) {
                return false;
            }
        }
        return true;
    }
}
