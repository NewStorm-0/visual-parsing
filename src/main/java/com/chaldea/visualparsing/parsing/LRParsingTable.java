package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;

public abstract class LRParsingTable {
    protected ActionItem[][] actionTable;
    protected ItemSet[][] gotoTable;

    public abstract Grammar getGrammar();

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