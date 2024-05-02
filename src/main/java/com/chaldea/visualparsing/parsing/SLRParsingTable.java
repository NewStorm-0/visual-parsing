package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.ArrayHelper;
import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import com.chaldea.visualparsing.grammar.*;

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
        lrCollection = lr0Collection;
        // +1是因为有结束标记#
        actionTable =
                new ActionItem[lr0Collection.size()][grammar.getTerminals().size() + 1];
        gotoTable = new ItemSet[lr0Collection.size()][grammar.getNonterminals().size()];
        initProductionSymbolsOrder(grammar);
        constructActionTable();
        constructGotoTable();
    }

    @Override
    public ActionItem action(int state, Terminal terminal) {
        int index = getSymbolNumber(terminal);
        return actionTable[state][index];
    }

    @Override
    public int go(int state, Nonterminal nonterminal) {
        int index = getSymbolNumber(nonterminal);
        ItemSet itemSet = gotoTable[state][index];
        return lr0Collection.getItemSetNumber(itemSet);
    }

    @Override
    public Terminal[] getActionColumnsHeader() {
        return terminalsOrder.clone();
    }

    @Override
    public Nonterminal[] getGotoColumnsHeader() {
        return nonterminalsOrder.clone();
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
     * @throws LRConflictException LR分析冲突
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
            ActionItem actionItem = new ActionItem(ActionItem.Action.SHIFT, goNumber);
            // 若表项不为空，且表项内容不同，则报错。
            if (actionTable[rowNumber][colNumber] != null
                    && !actionItem.equals(actionTable[rowNumber][colNumber])) {
                throw new LRConflictException(rowNumber, itemSet, (Terminal) symbol,
                        actionTable[rowNumber][colNumber], actionItem);
            }
            actionTable[rowNumber][colNumber] = actionItem;
        } else if (item.getHead().equals(lr0Collection.getAugmentedGrammar().getStartSymbol())) {
            // S'→S·
            int colNumber = getSymbolNumber(Terminal.END_MARKER);
            ActionItem actionItem = new ActionItem(ActionItem.Action.ACCEPT, -1);
            if (actionTable[rowNumber][colNumber] != null
                    && !actionItem.equals(actionTable[rowNumber][colNumber])) {
                throw new LRConflictException(rowNumber, itemSet, Terminal.END_MARKER,
                        actionTable[rowNumber][colNumber], actionItem);
            }
            actionTable[rowNumber][colNumber] = actionItem;
        } else {
            // A→α·
            LL1Parser ll1Parser = new LL1Parser(lr0Collection.getAugmentedGrammar());
            for (Terminal terminal : ll1Parser.follow(item.getHead())) {
                int colNumber = getSymbolNumber(terminal);
                ActionItem actionItem = new ActionItem(ActionItem.Action.REDUCE,
                        Grammars.getExpressionIndex(lr0Collection.getOriginalGrammar(),
                                item.getHead(), item.getExpression()));
                if (actionTable[rowNumber][colNumber] != null
                        && !actionItem.equals(actionTable[rowNumber][colNumber])) {
                    throw new LRConflictException(rowNumber, itemSet, terminal,
                            actionTable[rowNumber][colNumber], actionItem);
                }
                actionTable[rowNumber][colNumber] = actionItem;
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
        nonterminalsOrder = new Nonterminal[grammar.getNonterminals().size()];
        int index = 0;
        for (Terminal terminal : grammar.getTerminals()) {
            terminalsOrder[index] = terminal;
            index += 1;
        }
        terminalsOrder[index] = Terminal.END_MARKER;
        index = 0;
        for (Nonterminal nonterminal : grammar.getNonterminals()) {
            nonterminalsOrder[index] = nonterminal;
            index += 1;
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
            return ArrayHelper.findIndex(nonterminalsOrder, symbol);
        } else if (symbol instanceof Terminal) {
            return ArrayHelper.findIndex(terminalsOrder, symbol);
        } else {
            throw new UnknownSymbolException();
        }
    }

}


