package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class GrammarTest {
    private static final Logger logger = LoggerFactory.getLogger(GrammarTest.class);

    @Test
    void testExpression() {
        ProductionSymbol[] productionSymbols = new ProductionSymbol[]{
                new Nonterminal("A"), new Nonterminal("BEEE")
        };
        Expression expression = new Expression(productionSymbols);
        logger.info(expression.toString());
    }

    @Test
    void testGrammarTest() {
        Grammar grammar = new Grammar("START");
        grammar.addNonterminal("K");
        grammar.addNonterminal("DDD");
        grammar.addNonterminal("B");
        grammar.addTerminal("ok");
        grammar.addTerminal("hahaha");
        grammar.addExpression(new Nonterminal("DDD"), new Expression(
                new ProductionSymbol[]{new Nonterminal("B"), new Terminal("ok")}
        ));
        File file = new File(getClass().getResource("").getPath().substring(1) + "a.gra");
        try {
            GrammarReaderWriter.writeGrammarToFile(grammar, file);
        } catch (IOException e) {
            logger.error("写错误：", e);
        }
        try {
            URL url = getClass().getResource("a.gra");
            assert url != null;
            Grammar readGrammar =
                    GrammarReaderWriter.readGrammarFromFile(new File(url.getFile()));
            logger.info(readGrammar.toString());
        } catch (FileNotFoundException e) {
            logger.error("1", e);
        } catch (IOException e) {
            logger.error("2", e);
        }
    }

    @Test
    void test0() {
        String a = "0123";
        logger.info(a.substring(4));
    }
}
