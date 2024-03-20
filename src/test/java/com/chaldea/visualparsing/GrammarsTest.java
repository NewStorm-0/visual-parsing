package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrammarsTest {
    private static final Logger logger = LoggerFactory.getLogger(GrammarsTest.class);

    @Test
    void testEliminateLeftRecursion() {
        Grammar grammar = getLeftRecursionGrammar();
        Grammars.eliminateLeftRecursion(grammar);
        logger.info("Nonterminals:{}\nTerminals:{}\nProductions:\n{}",
                grammar.getNonterminals(), grammar.getTerminals(), grammar.getProductionsString());
    }

    @Test
    void testExtractingLeftCommonFactors() {
        Grammar grammar = getLeftFactoredGrammar();
        Grammars.extractingLeftCommonFactors(grammar);
        logger.info(grammar.getProductionsString());
    }

    private Grammar getLeftRecursionGrammar() {
        Grammar grammar = new Grammar("S");
        grammar.addNonterminal("A");
        grammar.addTerminal("a");
        grammar.addTerminal("b");
        grammar.addTerminal("c");
        grammar.addTerminal("d");
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
        return grammar;
    }

    private Grammar getLeftFactoredGrammar() {
        Grammar grammar = new Grammar("S");
        grammar.addNonterminal("E");
        grammar.addTerminal("i");
        grammar.addTerminal("t");
        grammar.addTerminal("e");
        grammar.addTerminal("a");
        grammar.addTerminal("b");
        grammar.addExpression("S", grammar.generateExpression("i", "E", "t", "S"));
        grammar.addExpression("S", grammar.generateExpression("i", "E", "t", "S", "e", "S"));
        grammar.addExpression("S", grammar.generateExpression("a"));
        grammar.addExpression("E", grammar.generateExpression("b"));
        return grammar;
    }
}
