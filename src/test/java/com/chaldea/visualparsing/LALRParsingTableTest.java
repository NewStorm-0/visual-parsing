package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.LALRParsingTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LALRParsingTableTest {
    private static Grammar grammar;
    private static final Logger logger =
            LoggerFactory.getLogger(LALRParsingTable.class);
    private LALRParsingTable lalrParsingTable;

    @BeforeAll
    public static void setGrammar() {
        grammar = new Grammar("S");
        grammar.addNonterminal("C");
        grammar.addTerminal("c");
        grammar.addTerminal("d");
        grammar.addExpression(grammar.getNonterminal("S"),
                grammar.generateExpression("C", "C"));
        grammar.addExpression(grammar.getNonterminal("C"),
                grammar.generateExpression("c", "C"));
        grammar.addExpression(grammar.getNonterminal("C"),
                grammar.generateExpression("d"));
    }

    @Test
    void test() {
        lalrParsingTable = new LALRParsingTable(grammar);
        logger.info("\n" + lalrParsingTable.getLrCollection());
        Terminal[] actionHeader = new Terminal[] {
                grammar.getTerminal("c"), grammar.getTerminal("d"),
                Terminal.END_MARKER
        };
        Nonterminal[] gotoHeader = new Nonterminal[] {
                grammar.getNonterminal("S"), grammar.getNonterminal("C")
        };
        logger.info("\n" + SLRParsingTableTest.lrParsingTableToString(lalrParsingTable,
                actionHeader, gotoHeader));
    }
}
