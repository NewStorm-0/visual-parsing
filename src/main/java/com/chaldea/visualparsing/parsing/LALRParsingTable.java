package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.grammar.Grammar;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LALRParsingTable extends LRParsingTable {

    private final LR1Collection lr1Collection;

    /**
     * 为了复用代码
     */
    private final LR1ParsingTable lr1ParsingTable;
    private static final Logger logger = LoggerFactory.getLogger(LALRParsingTable.class);

    public LALRParsingTable(Grammar grammar) {
        super(grammar);
        lr1Collection = new LR1Collection(grammar);
        lrCollection = lr1Collection;
        unionItemSets();
        logger.debug("LALR分析表的项集：\n" + lr1Collection);
        lr1ParsingTable = new LR1ParsingTable(grammar, lr1Collection);
        constructActionTable();
        gotoTable = new ItemSet[lr1Collection.size()][grammar.getNonterminals().size()];
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

    /**
     * 对于LR(1)项集中每个核心，找出所有具有这个核心的项集，并将这些项集替换为它们的并集
     */
    private void unionItemSets() {
        List<ItemSet> itemSetList = lr1Collection.itemSetList;
        // 新列表，用于存储完全合并后的项集
        List<ItemSet> finalLists = new ArrayList<>();
        // All elements are initialized to false by default
        boolean[] unionComputed = new boolean[itemSetList.size()];
        for (int i = 0; i < itemSetList.size(); i++) {
            if (unionComputed[i]) {
                continue;
            }
            ItemSet currentItemSet = itemSetList.get(i);
            for (int j = i + 1; j < itemSetList.size(); j++) {
                if (unionComputed[j]) {
                    continue;
                }
                ItemSet itemSet = itemSetList.get(j);
                if (!currentItemSet.hasSameCore(itemSet)) {
                    continue;
                }
                currentItemSet.union(itemSet);
                unionComputed[j] = true;
            }
            // 添加完全合并后的项集到新列表
            finalLists.add(currentItemSet);
        }
        itemSetList.clear();
        itemSetList.addAll(finalLists);
    }

    private void constructActionTable() {
        actionTable = lr1ParsingTable.getActionTable();
        // 需要重新生成SHIFT动作，因为GOTO(I,X)发生了变化
        // 清除原先的SHIFT动作
        for (ActionItem[] actionItems : actionTable) {
            for (int i = 0; i < actionItems.length; i++) {
                if (actionItems[i] == null) {
                    continue;
                }
                if (actionItems[i].action() == ActionItem.Action.SHIFT) {
                    actionItems[i] = null;
                }
            }
        }
        // 重新生成SHIFT动作
        for (ItemSet itemSet : lr1Collection) {
            itemSet.stream()
                    .map(n -> (LR1Item) n)
                    .filter(n -> n.getCurrentSymbol() instanceof Terminal)
                    .forEach(n -> setShiftActionItem(n, itemSet));
        }
    }

    private void setShiftActionItem(LR1Item lr1Item, ItemSet itemSet) {
        ItemSet goItemSet = lr1Collection.go(itemSet, lr1Item.getCurrentSymbol());
        int number = -2;
        for (ItemSet loopItemSet : lr1Collection) {
            if (loopItemSet.hasSameCore(goItemSet)) {
                number = lr1Collection.getItemSetNumber(loopItemSet);
                break;
            }
        }
        if (number == -2) {
            throw new LRConflictException("未找到有相同核心的项集");
        }
        ActionItem newActionItem = new ActionItem(ActionItem.Action.SHIFT, number);
        int rowNumber = lr1Collection.getItemSetNumber(itemSet);
        int colNumber = getSymbolNumber(lr1Item.getCurrentSymbol());
        ActionItem oldActionItem = actionTable[rowNumber][colNumber];
        if (oldActionItem != null && !oldActionItem.equals(newActionItem)) {
            throw new LRConflictException(rowNumber, itemSet,
                    (Terminal) lr1Item.getCurrentSymbol(), oldActionItem, newActionItem);
        }
        actionTable[rowNumber][colNumber] = newActionItem;
    }

    private void constructGotoTable() {
        for (ItemSet itemSet : lr1Collection.itemSetList) {
            for (Nonterminal nonterminal : getGrammar().getNonterminals()) {
                setGotoItem(itemSet, nonterminal);
            }
        }
    }

    private void setGotoItem(ItemSet itemSet, Nonterminal nonterminal) {
        int rowNumber = lr1Collection.getItemSetNumber(itemSet);
        int colNumber = getSymbolNumber(nonterminal);
        ItemSet goItemSet = lr1Collection.go(itemSet, nonterminal);
        if (goItemSet == null || goItemSet.isEmpty()) {
            return;
        }
        for (ItemSet loopItemSet : lr1Collection) {
            if (loopItemSet.hasSameCore(goItemSet)) {
                gotoTable[rowNumber][colNumber] = loopItemSet;
                return;
            }
        }
        throw new LRConflictException("未找到有相同核心的项集");
    }

}
