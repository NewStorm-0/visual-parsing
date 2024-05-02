package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.GrammarReaderWriter;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.LR0ParsingTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LR0ParsingTableTest {
    private static Grammar grammar;
    private static final Logger logger =
            LoggerFactory.getLogger(LR0ParsingTableTest.class);
    private LR0ParsingTable lr0ParsingTable;

    @BeforeAll
    public static void setGrammar() {
        grammar = new Grammar("S");
        grammar.addNonterminal("A");
        grammar.addNonterminal("B");
        grammar.addTerminal("a");
        grammar.addTerminal("b");
        grammar.addTerminal("c");
        grammar.addTerminal("d");
        grammar.addExpression("S", grammar.generateExpression("A"));
        grammar.addExpression("S", grammar.generateExpression("B"));
        grammar.addExpression("A", grammar.generateExpression("a", "A", "b"));
        grammar.addExpression("A", grammar.convertStringToExpression("c"));
        grammar.addExpression("B", grammar.convertStringToExpression("aBd"));
        grammar.addExpression("B", grammar.convertStringToExpression("d"));
        logger.debug(grammar.toString());
        generateGrammarFile();
    }

    @Test
    void test() {
        lr0ParsingTable = new LR0ParsingTable(grammar);
        Terminal[] actionHeader = new Terminal[]{
                grammar.getTerminal("a"), grammar.getTerminal("b"),
                grammar.getTerminal("c"), grammar.getTerminal("d"),
                Terminal.END_MARKER
        };
        Nonterminal[] gotoHeader = new Nonterminal[] {
                grammar.getNonterminal("S"), grammar.getNonterminal("A"),
                grammar.getNonterminal("B")
        };
        logger.info("\n" + SLRParsingTableTest.lrParsingTableToString(lr0ParsingTable,
                actionHeader, gotoHeader));
    }

    private static void generateGrammarFile() {
        try {
            GrammarReaderWriter.writeGrammarToFile(grammar, new File("./LR(0).gra"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
