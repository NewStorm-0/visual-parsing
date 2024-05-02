package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The type Canonical lr 0 collection.规范LR(0)项集族
 */
public class CanonicalLR0Collection extends LRCollection {
    /**
     * The Augmented grammar.增广文法
     */
    private Grammar augmentedGrammar;

    private static final Logger logger =
            LoggerFactory.getLogger(CanonicalLR0Collection.class);

    public CanonicalLR0Collection(Grammar grammar) {
        super(grammar);
        setAugmentedGrammar();
        setItemSetList();
        logger.debug("LR项集：\n" + this);
    }

    public List<ItemSet> getItemSetList() {
        return itemSetList;
    }

    public Grammar getAugmentedGrammar() {
        return augmentedGrammar;
    }

    public Grammar getOriginalGrammar() {
        return grammar;
    }

    /**
     * Gets go item set.
     * 返回的是itemSetList中的元素，而不是一个新的ItemSet对象
     *
     * @param itemSet the item set
     * @param symbol  the symbol
     * @return the go item set
     */
    public ItemSet getGoItemSet(ItemSet itemSet, ProductionSymbol symbol) {
        ItemSet goItemSet = go(itemSet, symbol);
        return itemSetList.get(getItemSetNumber(goItemSet));
    }

    /**
     * Gets go item set number.
     *
     * @param itemSet the item set
     * @param symbol  the symbol
     * @return the go item set number
     */
    public int getGoItemSetNumber(ItemSet itemSet, ProductionSymbol symbol) {
        return getItemSetNumber(go(itemSet, symbol));
    }

    /**
     * Gets closure item set.
     *
     * @param itemSet the item set
     * @return the closure item set
     */
    public ItemSet getClosureItemSet(ItemSet itemSet) {
        ItemSet closureItemSet = closure(itemSet);
        return itemSetList.get(getItemSetNumber(closureItemSet));
    }

    /**
     * Sets augmented grammar.生成增广文法
     * <p>如果G开始符号为S，那么G的增广文法G'就是在G中加上新开始符号S'和
     * 产生式S'→S而得到的文法</p>
     */
    private void setAugmentedGrammar() {
        augmentedGrammar = (Grammar) grammar.clone();
        // 加上新开始符号S'
        Nonterminal oldStartSymbol = grammar.getStartSymbol();
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
                        .generateExpression(grammar.getStartSymbol().toString()),
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
