package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.GrammarReaderWriter;
import com.chaldea.visualparsing.parsing.LR1Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LR1CollectionTest {
    private LR1Collection lr1Collection;
    private static Grammar grammar;
    private static final Logger logger = LoggerFactory.getLogger(LR1CollectionTest.class);

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
        generateGrammarFile();
    }

    @Test
    void testItemSetList() {
        lr1Collection = new LR1Collection(grammar);
        logger.info("\n" + lr1Collection);
    }

    private static void generateGrammarFile() {
        try {
            GrammarReaderWriter.writeGrammarToFile(grammar, new File("./龙书文法4.55.gra"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
