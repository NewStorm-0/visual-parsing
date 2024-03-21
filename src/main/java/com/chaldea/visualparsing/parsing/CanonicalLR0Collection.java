package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        setItemSetList();
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
     * Sets item set list.计算增广文法的规范LR(0)项族
     */
    private void setItemSetList() {
        ItemSet firstItemSet = closure(new Item(
                augmentedGrammar.getStartSymbol(),
                augmentedGrammar
                        .generateExpression(originalGrammar.getStartSymbol().toString()),
                0)
        );
        itemSetList.add(firstItemSet);
        List<ItemSet> addedItemSet = new ArrayList<>();
        addedItemSet.add(firstItemSet);
        while (!addedItemSet.isEmpty()) {
            List<ItemSet> addedItemSetCopy = new ArrayList<>(addedItemSet);
            addedItemSet.clear();
            for (ItemSet itemSet : addedItemSetCopy) {
                addGoForItem(itemSet, addedItemSet);
            }
        }
    }

    /**
     * Add go for item.
     *
     * @param itemSet      the item set
     * @param addedItemSet the added item set
     */
    private void addGoForItem(ItemSet itemSet, List<ItemSet> addedItemSet) {
        for (ProductionSymbol symbol : augmentedGrammar.getProductionSymbols()) {
            ItemSet goItemSet = go(itemSet, symbol);
            if (goItemSet.isEmpty() || itemSetList.contains(goItemSet)) {
                continue;
            }
            itemSetList.add(goItemSet);
            addedItemSet.add(goItemSet);
        }
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
     * Closure item set.计算一个项的闭包
     *
     * @param item the item
     * @return the item set
     */
    private ItemSet closure(Item item) {
        ItemSet itemSet = new ItemSet();
        itemSet.addItem(item);
        return closure(itemSet);
    }

    /**
     * GOTO(I,X)，返回I中所有形如[A→α·Xβ]的项[A→αX·β]的集合的闭包
     *
     * @param itemSet the item set.一个项集
     * @param symbol  the symbol.一个文法符号
     * @return the item set
     */
    private ItemSet go(ItemSet itemSet, ProductionSymbol symbol) {
        ItemSet goItemSet = new ItemSet();
        for (Item item : itemSet) {
            if (!symbol.equals(item.getCurrentSymbol())) {
                continue;
            }
            Item newItem = new Item(item.getHead(), item.getExpression(),
                    item.getPoint() + 1);
            goItemSet.addAllItems(closure(newItem));
        }
        return goItemSet;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < itemSetList.size(); i++) {
            stringBuilder.append("I").append(i).append(": ");
            stringBuilder.append(itemSetList.get(i));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
