package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Grammars;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;

public class LR1ParsingTable extends LRParsingTable {
    private final LR1Collection lr1Collection;

    public LR1ParsingTable(Grammar grammar) {
        super(grammar);
        lrCollection = new LR1Collection(grammar);
        lr1Collection = (LR1Collection) lrCollection;
        // +1是因为有结束标记#
        actionTable =
                new ActionItem[lr1Collection.size()][grammar.getTerminals().size() + 1];
        gotoTable = new ItemSet[lr1Collection.size()][grammar.getNonterminals().size()];
        constructActionTable();
        constructGotoTable();
    }

    @Override
    public ActionItem action(int state, Terminal terminal) {
        return actionTable[state][getSymbolNumber(terminal)];
    }

    @Override
    public int go(int state, Nonterminal nonterminal) {
        int index = getSymbolNumber(nonterminal);
        ItemSet itemSet = gotoTable[state][index];
        return lr1Collection.getItemSetNumber(itemSet);
    }

    private void constructActionTable() {
        for (ItemSet itemSet : lr1Collection.itemSetList) {
            for (Item item : itemSet) {
                LR1Item lr1Item = (LR1Item) item;
                setActionItem(lr1Item, itemSet);
            }
        }
    }

    private void setActionItem(LR1Item lr1Item, ItemSet itemSet) {
        int rowNumber = lr1Collection.getItemSetNumber(itemSet);
        int colNumber;
        if (lr1Item.getCurrentSymbol() != null) {
            if (!(lr1Item.getCurrentSymbol() instanceof Terminal terminal)) {
                return;
            }
            // A→α·aβ, b   a is a terminal
            colNumber = getSymbolNumber(terminal);
            ActionItem actionItem = new ActionItem(ActionItem.Action.SHIFT,
                    lr1Collection.getGoItemSetNumber(itemSet, terminal));
            setActionItem(rowNumber, colNumber, actionItem);
        } else if (lr1Item.getHead().equals(lr1Collection.getAugmentedGrammar().getStartSymbol())) {
            // S'→S·,#
            colNumber = getSymbolNumber(Terminal.END_MARKER);
            ActionItem actionItem = new ActionItem(ActionItem.Action.ACCEPT, -1);
            setActionItem(rowNumber, colNumber, actionItem);
        } else {
            // A→α·,a and A≠S'
            colNumber = getSymbolNumber(lr1Item.getLookahead());
            ActionItem actionItem = new ActionItem(ActionItem.Action.REDUCE,
                    Grammars.getExpressionIndex(getGrammar(), lr1Item.getHead(),
                            lr1Item.getExpression()));
            setActionItem(rowNumber, colNumber, actionItem);
        }
    }

    /**
     * 设置actionTable项，并检查设置actionTable项时是否有冲突
     *
     * @param rowNumber  the row number
     * @param colNumber  the col number
     * @param actionItem the action item
     * @throws LRConflictException LR分析表构造冲突
     */
    private void setActionItem(int rowNumber, int colNumber, ActionItem actionItem) {
        if (actionTable[rowNumber][colNumber] != null
                && !actionTable[rowNumber][colNumber].equals(actionItem)) {
            ItemSet itemSet = lr1Collection.get(rowNumber);
            Terminal terminal = terminalsOrder[colNumber];
            throw new LRConflictException(rowNumber, itemSet, terminal,
                    actionTable[rowNumber][colNumber], actionItem);
        }
        actionTable[rowNumber][colNumber] = actionItem;
    }

    private void constructGotoTable() {
        for (ItemSet itemSet : lr1Collection.itemSetList) {
            int rowNumber = lr1Collection.getItemSetNumber(itemSet);
            getGrammar().getNonterminals().forEach(n -> {
                int colNumber = getSymbolNumber(n);
                gotoTable[rowNumber][colNumber] =
                        lr1Collection.getGoItemSet(itemSet, n);
            });
        }
    }

}
