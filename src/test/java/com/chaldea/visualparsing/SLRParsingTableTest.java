package com.chaldea.visualparsing;

import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.ActionItem;
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
        StringBuilder stringBuilder = new StringBuilder(512);
        Terminal[] actionHeader = new Terminal[] {
                grammar.getTerminal("id"), grammar.getTerminal("+"),
                grammar.getTerminal("*"), grammar.getTerminal("("),
                grammar.getTerminal(")"), Terminal.END_MARKER
        };
        Nonterminal[] gotoHeader = new Nonterminal[] {
                grammar.getNonterminal("E"), grammar.getNonterminal("T"),
                grammar.getNonterminal("F")
        };
        stringBuilder.append(" ").append('\t');
        for (Terminal terminal : actionHeader) {
            stringBuilder.append(terminal.getValue()).append('\t');
        }
        for (Nonterminal nonterminal : gotoHeader) {
            stringBuilder.append(nonterminal.getValue()).append('\t');
        }
        stringBuilder.append('\n');
        for (int i = 0; i < slrParsingTable.getLrCollection().size(); i++) {
            stringBuilder.append(i).append('\t');
            for (Terminal terminal : actionHeader) {
                stringBuilder.append(convertActionItemToString(slrParsingTable.action(i
                        , terminal))).append('\t');
            }
            for (Nonterminal nonterminal : gotoHeader) {
                int value = slrParsingTable.go(i, nonterminal);
                if (value != -1) {
                    stringBuilder.append(value);
                }
                stringBuilder.append('\t');
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append('\n');
        }
        logger.info('\n' + stringBuilder.toString());
    }

    private static String convertActionItemToString(ActionItem actionItem) {
        if (actionItem == null) {
            return "";
        }
        if (actionItem.action() == ActionItem.Action.ACCEPT) {
            return "acc";
        }
        if (actionItem.action() == ActionItem.Action.REDUCE) {
            return "r" + actionItem.number();
        }
        if (actionItem.action() == ActionItem.Action.SHIFT) {
            return "s" + actionItem.number();
        }
        return String.valueOf(actionItem.number());
    }


}
