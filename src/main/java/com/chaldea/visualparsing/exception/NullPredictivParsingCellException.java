package com.chaldea.visualparsing.exception;

import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;

/**
 * LL1语法分析，没在预测分析表中找到相应表项时抛出的异常
 */
public class NullPredictivParsingCellException extends BaseException{

    private Nonterminal nonterminal;

    private Terminal symbol;

    public Nonterminal getNonterminal() {
        return nonterminal;
    }

    public void setNonterminal(Nonterminal nonterminal) {
        this.nonterminal = nonterminal;
    }

    public Terminal getSymbol() {
        return symbol;
    }

    public void setSymbol(Terminal symbol) {
        this.symbol = symbol;
    }

    public NullPredictivParsingCellException(Nonterminal nonterminal, Terminal symbol) {
        this.nonterminal = nonterminal;
        this.symbol = symbol;
    }

    public NullPredictivParsingCellException(String message, Nonterminal nonterminal, Terminal symbol) {
        super(message);
        this.nonterminal = nonterminal;
        this.symbol = symbol;
    }
}
