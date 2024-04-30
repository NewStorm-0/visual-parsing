package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.GrammarReaderWriter;
import com.chaldea.visualparsing.parsing.CanonicalLR0Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CanonicalLR0CollectionTest {

    private static Grammar grammar;
    private CanonicalLR0Collection lr0Collection;
    private static final Logger logger =
            LoggerFactory.getLogger(CanonicalLR0CollectionTest.class);

    @BeforeAll
    public static void setGrammar() {
        grammar = new Grammar("E");
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
        generateGrammarFile();
    }

    @Test
    void testItemSetList() {
        lr0Collection = new CanonicalLR0Collection(grammar);
        logger.info("\n" + lr0Collection);
    }

    private static void generateGrammarFile() {
        try {
            GrammarReaderWriter.writeGrammarToFile(grammar, new File("./龙书文法4.1.gra"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
