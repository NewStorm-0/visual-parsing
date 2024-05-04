package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.ArrayHelper;
import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.ProductionSymbol;
import com.chaldea.visualparsing.grammar.Terminal;

public abstract class LRParsingTable {
    protected ActionItem[][] actionTable;
    protected ItemSet[][] gotoTable;
    protected LRCollection lrCollection;
    /**
     * 记录终结符符号的顺序
     */
    protected Terminal[] terminalsOrder;
    /**
     * 记录非终结符符号的顺序
     */
    protected Nonterminal[] nonterminalsOrder;

    public LRParsingTable(Grammar grammar) {
        if (grammar.isEmpty()) {
            throw new BaseException("grammar 为 empty");
        }
        initProductionSymbolsOrder(grammar);
    }

    public Grammar getGrammar() {
        return lrCollection.getGrammar();
    }

    public LRCollection getLrCollection() {
        return lrCollection;
    }

    public ActionItem[][] getActionTable() {
        return actionTable;
    }

    public ItemSet[][] getGotoTable() {
        return gotoTable;
    }

    public abstract ActionItem action(int state, Terminal terminal);

    public abstract int go(int state, Nonterminal nonterminal);

    public Terminal[] getActionColumnsHeader() {
        return terminalsOrder.clone();
    }

    public Nonterminal[] getGotoColumnsHeader() {
        return nonterminalsOrder.clone();
    }

    /**
     * Gets symbol number.获取文法符号对应的序号
     *
     * @param symbol the symbol
     * @return the symbol number
     */
    protected int getSymbolNumber(ProductionSymbol symbol) {
        if (symbol instanceof Nonterminal) {
            return ArrayHelper.findIndex(nonterminalsOrder, symbol);
        } else if (symbol instanceof Terminal) {
            return ArrayHelper.findIndex(terminalsOrder, symbol);
        } else {
            throw new UnknownSymbolException();
        }
    }

    /**
     * 初始化表格每列符号的顺序
     *
     * @param grammar the grammar
     */
    private void initProductionSymbolsOrder(Grammar grammar) {
        terminalsOrder = new Terminal[grammar.getTerminals().size() + 1];
        nonterminalsOrder = new Nonterminal[grammar.getNonterminals().size()];
        int index = 0;
        for (Terminal terminal : grammar.getTerminals()) {
            terminalsOrder[index] = terminal;
            index += 1;
        }
        terminalsOrder[index] = Terminal.END_MARKER;
        index = 0;
        for (Nonterminal nonterminal : grammar.getNonterminals()) {
            nonterminalsOrder[index] = nonterminal;
            index += 1;
        }
    }

    /**
     * The enum Type. 具体的LR类型
     */
    public enum Type {
        SLR, LR0, LR1, LALR;
    }
}