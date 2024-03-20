package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The type Canonical lr 0 collection.规范LR(0)项集族
 */
public class CanonicalLR0Collection {
    /**
     * The Augmented grammar.增广文法
     */
    private Grammar augmentedGrammar;

    /**
     * The Original grammar.原先的文法
     */
    private final Grammar originalGrammar;

    private final List<ItemSet> itemSetList;

    private static final Logger logger =
            LoggerFactory.getLogger(CanonicalLR0Collection.class);

    public CanonicalLR0Collection(Grammar grammar) {
        originalGrammar = grammar;
        itemSetList = new ArrayList<>();
        setAugmentedGrammar();
    }

    /**
     * Sets augmented grammar.生成增广文法
     * <p>如果G开始符号为S，那么G的增广文法G'就是在G中加上新开始符号S'和
     * 产生式S'→S而得到的文法</p>
     */
    private void setAugmentedGrammar() {
        augmentedGrammar = (Grammar) originalGrammar.clone();
        // 加上新开始符号S'
        Nonterminal oldStartSymbol = originalGrammar.getStartSymbol();
        Nonterminal newStartSymbol = Grammars.getAuxiliaryNonterminal(augmentedGrammar,
                augmentedGrammar.getStartSymbol());
        augmentedGrammar.addNonterminal(newStartSymbol);
        augmentedGrammar.setStartSymbol(newStartSymbol);
        // 加上产生式S'→S
        augmentedGrammar.addExpression(newStartSymbol,
                new Expression(new ProductionSymbol[]{oldStartSymbol}));
    }

    /**
     * Closure item set.计算项集的闭包
     *
     * @param itemSet the item set 初始项集I
     * @return the item set 项集的闭包
     */
    private ItemSet closure(ItemSet itemSet) {
        if (itemSet == null || itemSet.isEmpty()) {
            return new ItemSet();
        }
        ItemSet closureSet = new ItemSet();
        closureSet.addAllItems(itemSet);
        // 被加入的新的Item
        ItemSet newAddedItemSet = new ItemSet();
        newAddedItemSet.addAllItems(closureSet);
        while (!newAddedItemSet.isEmpty()) {
            Set<Item> setCopy = newAddedItemSet.getItemsCopy();
            newAddedItemSet.clear();
            for (Item item : setCopy) {
                addItemsBasedOnItem(item, closureSet, newAddedItemSet);
            }
        }
        return closureSet;
    }

    /**
     * Add new items to closure item set.
     *
     * @param item            the item which is based on
     * @param closureSet      the closure set
     * @param newAddedItemSet the new added item set
     * @return the item set
     */
    private void addItemsBasedOnItem(Item item, ItemSet closureSet,
                                     ItemSet newAddedItemSet) {
        ProductionSymbol symbol = item.getCurrentSymbol();
        if (symbol == null) {
            return;
        }
        if (!(symbol instanceof Nonterminal head)) {
            return;
        }
        Production production = Grammars.getProductionByHead(head,
                augmentedGrammar);
        for (Expression expression : production.getBody()) {
            expression = expression.copy();
            Item newItem = new Item(production.getHead(), expression, 0);
            if (closureSet.addItem(newItem)) {
                newAddedItemSet.addItem(newItem);
            }
        }
    }

}
