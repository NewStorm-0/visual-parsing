package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.parsing.LL1Parser;
import com.chaldea.visualparsing.parsing.PredictiveParsingTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LL1ParserTest {
    private static final Logger logger = LoggerFactory.getLogger(LL1ParserTest.class);

    private static Grammar grammar;

    private LL1Parser ll1Parser;

    @BeforeAll
    public static void setGrammar() {
        grammar = new Grammar("E");
        Terminal[] terminals = new Terminal[] {
                new Terminal("+"),
                new Terminal("*"), new Terminal("("),
                new Terminal(")"), new Terminal("id")
        };
        for (Terminal t : terminals) {
            grammar.addTerminal(t);
        }
        Nonterminal[] nonterminals = new Nonterminal[] {
                new Nonterminal("T"), new Nonterminal("E'"),
                new Nonterminal("T'"), new Nonterminal("F")
        };
        for (Nonterminal nt : nonterminals) {
            grammar.addNonterminal(nt);
        }
        grammar.addExpression(
                grammar.getStartSymbol(),
                new Expression(new ProductionSymbol[]{
                        new Nonterminal("T"), new Nonterminal("E'")
                })
        );
        grammar.addExpression(
                new Nonterminal("E'"),
                new Expression(new ProductionSymbol[]{
                        new Terminal("+"), new Nonterminal("T"),
                        new Nonterminal("E'")
                })
        );
        grammar.addExpression(
                new Nonterminal("E'"), new Expression(new ProductionSymbol[]{
                        Terminal.EMPTY_STRING
                })
        );
        grammar.addExpression(new Nonterminal("T"), new Expression(new ProductionSymbol[]{
                new Nonterminal("F"), new Nonterminal("T'")
        }));
        grammar.addExpression(new Nonterminal("T'"), new Expression(new ProductionSymbol[]{
                new Terminal("*"), new Nonterminal("F"),
                new Nonterminal("T'")
        }));
        grammar.addExpression(new Nonterminal("T'"), new Expression(new ProductionSymbol[]{
                Terminal.EMPTY_STRING
        }));
        grammar.addExpression(new Nonterminal("F"), new Expression(new ProductionSymbol[]{
                new Terminal("("), new Nonterminal("E") ,new Terminal(")")
        }));
        grammar.addExpression(new Nonterminal("F"), new Expression(new ProductionSymbol[]{
                new Terminal("id")
        }));
    }

    @BeforeEach
    public void setLl1Parser() {
        ll1Parser = new LL1Parser(grammar);
    }

    @Test
    void saveGrammarToFile() {
        try {
            GrammarReaderWriter.writeGrammarToFile(grammar, new File("./example.gra"));
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    @Test
    void testFirst() {
        logger.info(ll1Parser.getGrammar().getProductions().toString());
        Set<Terminal> fFirst = new HashSet<>();
        fFirst.add(grammar.getTerminal("("));
        fFirst.add(grammar.getTerminal("id"));
        assertEquals(fFirst, ll1Parser.first(grammar.getNonterminal("F")));
        assertEquals(fFirst, ll1Parser.first(grammar.getNonterminal("T")));
        assertEquals(fFirst, ll1Parser.first(grammar.getNonterminal("E")));

        Set<Terminal> e1First = new HashSet<>();
        e1First.add(grammar.getTerminal("+"));
        e1First.add(Terminal.EMPTY_STRING);
        assertEquals(e1First, ll1Parser.first(grammar.getNonterminal("E'")));
        assertEquals(e1First, ll1Parser.first(new Expression(new ProductionSymbol[]{
                grammar.getNonterminal("E'")
        })));

        Set<Terminal> t1First = new HashSet<>();
        t1First.add(grammar.getTerminal("*"));
        t1First.add(Terminal.EMPTY_STRING);
        assertEquals(t1First, ll1Parser.first(grammar.getNonterminal("T'")));
    }


    @Test
    void testFollow() {
        Set<Terminal> eFollow = new HashSet<>();
        eFollow.add(grammar.getTerminal(")"));
        eFollow.add(Terminal.END_MARKER);
        assertEquals(eFollow, ll1Parser.follow(grammar.getNonterminal("E")));
        assertEquals(eFollow, ll1Parser.follow(grammar.getNonterminal("E'")));

        Set<Terminal> tFollow = new HashSet<>();
        tFollow.add(grammar.getTerminal("+"));
        tFollow.add(grammar.getTerminal(")"));
        tFollow.add(Terminal.END_MARKER);
        assertEquals(tFollow, ll1Parser.follow(grammar.getNonterminal("T")));
        assertEquals(tFollow, ll1Parser.follow(grammar.getNonterminal("T'")));

        Set<Terminal> fFollow = new HashSet<>();
        fFollow.add(grammar.getTerminal("+"));
        fFollow.add(grammar.getTerminal("*"));
        fFollow.add(grammar.getTerminal(")"));
        fFollow.add(Terminal.END_MARKER);
        assertEquals(fFollow, ll1Parser.follow(grammar.getNonterminal("F")));
    }

    @Test
    void testGeneratePredictiveParsingTable() {
        PredictiveParsingTable table = ll1Parser.generatePredictiveParsingTable();
        logger.info("row map:\n{}", table.getNonterminalMap());
        logger.info("col map:\n{}", table.getInputSymbolMap());
        logger.info("table:\n{}", table.toFormattedTableString());
    }
}
