package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;

public abstract class LRParsingTable {
    protected ActionItem[][] actionTable;
    protected ItemSet[][] gotoTable;
    protected LRCollection lrCollection;

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

    public abstract Terminal[] getActionColumnsHeader();

    public abstract Nonterminal[] getGotoColumnsHeader();

    /**
     * The enum Type. 具体的LR类型
     */
    public enum Type {
        SLR, LR0, LR1, LALR;
    }
}