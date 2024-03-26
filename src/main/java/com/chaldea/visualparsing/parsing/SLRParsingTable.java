package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.SLRConflictException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import com.chaldea.visualparsing.grammar.*;

import java.util.Arrays;

/**
 * The type Slr parsing table.SLR语法分析表
 */
public class SLRParsingTable extends LRParsingTable {
    private Terminal[] terminalsOrder;
    private Nonterminal[] nonterminalsOrder;
    private final CanonicalLR0Collection lr0Collection;

    public SLRParsingTable(Grammar grammar) {
        if (grammar.isEmpty()) {
            throw new BaseException("grammar 为 empty");
        }
        lr0Collection = new CanonicalLR0Collection(grammar);
        // +1是因为有结束标记#
        actionTable =
                new ActionItem[lr0Collection.size()][grammar.getTerminals().size() + 1];
        gotoTable = new ItemSet[lr0Collection.size()][grammar.getNonterminals().size()];
        initProductionSymbolsOrder(grammar);
        constructActionTable();
        constructGotoTable();
    }

    @Override
    public Grammar getGrammar() {
        return lr0Collection.getGrammar();
    }

    @Override
    public ActionItem action(int state, Terminal terminal) {
        return null;
    }

    @Override
    public int go(int state, Nonterminal nonterminal) {
        return 0;
    }

    /**
     * Construct action table.构建ACTION表
     */
    private void constructActionTable() {
        for (ItemSet itemSet : lr0Collection) {
            for (Item item : itemSet) {
                generateActionItem(item, itemSet);
            }
        }
    }

    /**
     * Generate action item.填充actionTable
     *
     * @param item    the item
     * @param itemSet the item set
     * @throws SLRConflictException SLR分析冲突
     */
    private void generateActionItem(Item item, ItemSet itemSet) {
        ProductionSymbol symbol = item.getCurrentSymbol();
        int rowNumber = lr0Collection.getItemSetNumber(itemSet);
        if (symbol != null) {
            // A→α·aβ
            if (!(symbol instanceof Terminal)) {
                return;
            }
            int goNumber = lr0Collection.getGoItemSetNumber(itemSet, item.getCurrentSymbol());
            int colNumber = getSymbolNumber(symbol);
            if (actionTable[rowNumber][colNumber] != null) {
                throw new SLRConflictException(rowNumber, itemSet, (Terminal) symbol);
            }
            actionTable[rowNumber][colNumber] = new ActionItem(ActionItem.Action.SHIFT,
                    goNumber);
        } else if (item.getHead().equals(lr0Collection.getAugmentedGrammar().getStartSymbol())) {
            // S'→S·
            int colNumber = getSymbolNumber(Terminal.END_MARKER);
            if (actionTable[rowNumber][colNumber] != null) {
                throw new SLRConflictException(rowNumber, itemSet, (Terminal) symbol);
            }
            actionTable[rowNumber][colNumber] = new ActionItem(ActionItem.Action.ACCEPT
                    , -1);
        } else {
            // A→α·
            LL1Parser ll1Parser = new LL1Parser(lr0Collection.getAugmentedGrammar());
            for (Terminal terminal : ll1Parser.follow(item.getHead())) {
                int colNumber = getSymbolNumber(terminal);
                if (actionTable[rowNumber][colNumber] != null) {
                    throw new SLRConflictException(rowNumber, itemSet, (Terminal) symbol);
                }
                actionTable[rowNumber][colNumber] =
                        new ActionItem(ActionItem.Action.REDUCE,
                        Grammars.getExpressionIndex(lr0Collection.getOriginalGrammar(),item.getHead(), item.getExpression())
                );
            }
        }
    }

    /**
     * Generate goto.填充gotoTable
     */
    private void constructGotoTable() {
        for (ItemSet itemSet : lr0Collection) {
            int rowNumber = lr0Collection.getItemSetNumber(itemSet);
            for (Nonterminal nonterminal :
                    lr0Collection.getOriginalGrammar().getNonterminals()) {
                int colNumber = getSymbolNumber(nonterminal);
                try {
                    gotoTable[rowNumber][colNumber] =
                            lr0Collection.getGoItemSet(itemSet, nonterminal);
                } catch (IndexOutOfBoundsException e) {
                    gotoTable[rowNumber][colNumber] = null;
                }
            }
        }
    }

    private void initProductionSymbolsOrder(Grammar grammar) {
        terminalsOrder = new Terminal[grammar.getTerminals().size() + 1];
        nonterminalsOrder = new Nonterminal[grammar.getTerminals().size()];
        int index = 0;
        for (Terminal terminal : grammar.getTerminals()) {
            terminalsOrder[index] = terminal;
            index += 1;
        }
        terminalsOrder[index] = Terminal.END_MARKER;
        index = 0;
        for (Nonterminal nonterminal : grammar.getNonterminals()) {
            nonterminalsOrder[index] = nonterminal;
        }
    }

    /**
     * Gets symbol number.获取文法符号对应的序号
     *
     * @param symbol the symbol
     * @return the symbol number
     */
    private int getSymbolNumber(ProductionSymbol symbol) {
        if (symbol instanceof Nonterminal) {
            return Arrays.binarySearch(nonterminalsOrder, symbol);
        } else if (symbol instanceof Terminal) {
            return Arrays.binarySearch(terminalsOrder, symbol);
        } else {
            throw new UnknownSymbolException();
        }
    }

}


