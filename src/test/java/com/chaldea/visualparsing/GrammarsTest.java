package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrammarsTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void testEliminateLeftRecursion() {
        Grammar grammar = new Grammar("S");
        grammar.addNonterminal("A");
        grammar.addTerminal("a");
        grammar.addTerminal("b");
        grammar.addTerminal("c");
        grammar.addTerminal("d");
        grammar.addTerminal(Terminal.EMPTY_STRING);
        grammar.addExpression(grammar.getStartSymbol(), new Expression(new ProductionSymbol[]{
                new Nonterminal("A"), new Terminal("a")
        }));
        grammar.addExpression(grammar.getStartSymbol(), new Expression(new ProductionSymbol[]{
                new Terminal("b")
        }));
        grammar.addExpression(grammar.getNonterminal("A"), new Expression(new ProductionSymbol[]{
                grammar.getNonterminal("A"), grammar.getTerminal("c")
        }));
        grammar.addExpression(grammar.getNonterminal("A"), grammar.convertStringToExpression(
                "Sd"
        ));
        grammar.addExpression(grammar.getNonterminal("A"), new Expression(new ProductionSymbol[]{
                Terminal.EMPTY_STRING
        }));

        Grammars.eliminateLeftRecursion(grammar);
        logger.info("Nonterminals:{}\nTerminals:{}\nProductions:\n{}",
                grammar.getNonterminals(), grammar.getTerminals(), grammar.getProductionsString());
    }
}
