package com.chaldea.visualparsing.grammar;

import com.chaldea.visualparsing.exception.grammar.EmptyHeadProductionException;
import com.chaldea.visualparsing.exception.grammar.IllegalSymbolException;
import com.chaldea.visualparsing.exception.grammar.RepeatedSymbolException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文法
 */
public class Grammar implements Serializable {
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
        productions = new ArrayList<>(8);
        nonterminals = new HashSet<>(8);
        terminals = new HashSet<>(8);
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
     */
    public void addTerminal(Terminal symbol) {
        if (terminals.contains(symbol)) {
            throw new RepeatedSymbolException("已经包含该终结符");
        }
        if (nonterminals.contains(new Nonterminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("非终结符集合中已经包含该符号");
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
     */
    public void addNonterminal(Nonterminal symbol) {
        if (terminals.contains(new Terminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("终结符集合已经包含该符号");
        }
        if (nonterminals.contains(new Nonterminal(symbol.getValue()))) {
            throw new RepeatedSymbolException("已经包含该非终结符");
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
     */
    public void addExpression(Nonterminal head, Expression exp) throws EmptyHeadProductionException {
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

    /**
     * 检查表达式中符号是否已经被包含在相应的符号集合中
     * @param expression 表达式
     * @return 如果都包含则返回 true，否则返回 false
     */
    private boolean checkExpressionSymbols(Expression expression) {
        for (ProductionSymbol symbol : expression.getValue()) {
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
