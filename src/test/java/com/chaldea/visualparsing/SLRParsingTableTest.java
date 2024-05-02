package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.ActionItem;
import com.chaldea.visualparsing.parsing.LRParsingTable;
import com.chaldea.visualparsing.parsing.SLRParsingTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLRParsingTableTest {
    private static Grammar grammar;
    private static final Logger logger =
            LoggerFactory.getLogger(SLRParsingTableTest.class);
    private SLRParsingTable slrParsingTable;

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
    }

    @Test
    void test() {
        slrParsingTable = new SLRParsingTable(grammar);
        Terminal[] actionHeader = new Terminal[] {
                grammar.getTerminal("id"), grammar.getTerminal("+"),
                grammar.getTerminal("*"), grammar.getTerminal("("),
                grammar.getTerminal(")"), Terminal.END_MARKER
        };
        Nonterminal[] gotoHeader = new Nonterminal[] {
                grammar.getNonterminal("E"), grammar.getNonterminal("T"),
                grammar.getNonterminal("F")
        };
        logger.info('\n' + lrParsingTableToString(slrParsingTable, actionHeader, gotoHeader));
    }

    static String lrParsingTableToString(LRParsingTable table, Terminal[] actionHeader,
                                         Nonterminal[] gotoHeader) {
        StringBuilder stringBuilder = new StringBuilder(512);
        stringBuilder.append(" ").append('\t');
        for (Terminal terminal : actionHeader) {
            stringBuilder.append(terminal.getValue()).append('\t');
        }
        for (Nonterminal nonterminal : gotoHeader) {
            stringBuilder.append(nonterminal.getValue()).append('\t');
        }
        stringBuilder.append('\n');
        for (int i = 0; i < table.getLrCollection().size(); i++) {
            stringBuilder.append(i).append('\t');
            for (Terminal terminal : actionHeader) {
                stringBuilder.append(ActionItem.toString(
                        table.action(i, terminal))).append('\t');
            }
            for (Nonterminal nonterminal : gotoHeader) {
                int value = table.go(i, nonterminal);
                if (value != -1) {
                    stringBuilder.append(value);
                }
                stringBuilder.append('\t');
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

}
