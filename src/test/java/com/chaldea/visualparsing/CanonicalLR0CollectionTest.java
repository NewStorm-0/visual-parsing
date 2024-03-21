package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.parsing.CanonicalLR0Collection;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanonicalLR0CollectionTest {

    private CanonicalLR0Collection lr0Collection;
    private static final Logger logger =
            LoggerFactory.getLogger(CanonicalLR0CollectionTest.class);

    private Grammar getGrammar() {
        Grammar grammar = new Grammar("E");
        grammar.addNonterminal("T");
        grammar.addNonterminal("F");
        grammar.addTerminal("+");
        grammar.addTerminal("*");
        grammar.addTerminal("(");
        grammar.addTerminal(")");
        grammar.addTerminal("id");
        grammar.addExpression("E", grammar.generateExpression("E", "+", "T"));
        grammar.addExpression("E", grammar.generateExpression("T"));
        grammar.addExpression("T", grammar.generateExpression("T", "*", "F"));
        grammar.addExpression("T", grammar.generateExpression("F"));
        grammar.addExpression("F", grammar.generateExpression("(", "E", ")"));
        grammar.addExpression("F", grammar.generateExpression("id"));
        return grammar;
    }

    @Test
    void testItemSetList() {
        lr0Collection = new CanonicalLR0Collection(getGrammar());
        logger.info("\n" + lr0Collection);
    }

}
