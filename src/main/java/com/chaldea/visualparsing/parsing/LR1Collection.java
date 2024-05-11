package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.BaseException;
import com.chaldea.visualparsing.grammar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LR1Collection extends LRCollection {
    /**
     * The Augmented grammar.增广文法
     */
    private final Grammar augmentedGrammar;
    private static final Logger logger = LoggerFactory.getLogger(LR1Collection.class);

    public LR1Collection(Grammar grammar) {
        super(grammar);
        augmentedGrammar = Grammars.getAugmentedGrammar(grammar);
        setItems();
        logger.debug("LR(1)项集：\n" + this);
    }

    public Grammar getAugmentedGrammar() {
        return augmentedGrammar;
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
        int index = getItemSetNumber(goItemSet);
        return index == -1 ? null : itemSetList.get(index);
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

    ItemSet closure(ItemSet itemSet) {
        ItemSet closureItemSet = new ItemSet(itemSet.getItemsCopy());
        ItemSet lastStepItemSet;
        do {
            lastStepItemSet = new ItemSet(closureItemSet.getItemsCopy());
            for (Item item : closureItemSet.getItemsCopy()) {
                // 筛选到项 A→α·Bβ,a
                // 此处使用了模式匹配特性
                if (!(item instanceof LR1Item lr1Item)) {
                    throw new BaseException("不是LR1项集");
                }
                if (lr1Item.getCurrentSymbol() == null) {
                    continue;
                }
                if (!(lr1Item.getCurrentSymbol() instanceof Nonterminal currentSymbol)) {
                    continue;
                }
                // βa
                List<ProductionSymbol> symbolList = new ArrayList<>();
                for (int i = lr1Item.getPoint() + 1; i < lr1Item.getExpression().length(); i++) {
                    symbolList.add(lr1Item.getExpression().get(i));
                }
                symbolList.add(lr1Item.getLookahead());
                Expression symbols = new Expression(symbolList);
                // 获取B为头的所有产生式
                Production production = Grammars.getProductionByHead(currentSymbol,
                        augmentedGrammar);
                addClosureItems(symbols, production, closureItemSet);
            }
        } while (!lastStepItemSet.equals(closureItemSet));
        return closureItemSet;
    }

    ItemSet closure(LR1Item lr1Item) {
        ItemSet itemSet = new ItemSet();
        itemSet.addItem(lr1Item);
        return closure(itemSet);
    }

    /**
     * 对于G'每个产生式 B→γ ，将 B→·γ,b 加入到集合 itemSet 中，其中 b 是
     * FIRST(βa) 中每一个终结符号
     *
     * @param symbols    代表 βa
     * @param production G'中 B 为头的产生式
     * @param itemSet    the item set
     */
    private void addClosureItems(Expression symbols, Production production,
                                 ItemSet itemSet) {
        for (Expression expression : production.getBody()) {
            // 新建 LL1Parser 对象是为了获取 FIRST(βa)
            LL1Parser ll1Parser = new LL1Parser(augmentedGrammar);
            Set<Terminal> terminals = ll1Parser.first(symbols);
            for (Terminal terminal : terminals) {
                LR1Item lr1Item = new LR1Item(production.getHead(), expression,
                        0, terminal);
                itemSet.addItem(lr1Item);
            }
        }
    }

    ItemSet go(ItemSet itemSet, ProductionSymbol symbol) {
        ItemSet kernelItemSet = new ItemSet();
        itemSet.getItemsCopy()
                .stream()
                .filter(n -> symbol.equals(n.getCurrentSymbol()))
                .map(n -> new LR1Item(n.getHead(), n.getExpression(),
                        n.getPoint() + 1, ((LR1Item) n).getLookahead()))
                .forEach(kernelItemSet::addItem);
        return closure(kernelItemSet);
    }

    /**
     * Sets items.构造LR(1)项集族
     */
    private void setItems() {
        // 将C初始化为{CLOSURE(S'→·S,$)}
        ItemSet initialItemSet = new ItemSet();
        initialItemSet.addItem(new LR1Item(
                augmentedGrammar.getStartSymbol(),
                Grammars.getProductionByHead(augmentedGrammar.getStartSymbol(),
                        augmentedGrammar).getBody().get(0),
                0,
                Terminal.END_MARKER
        ));
        itemSetList.add(closure(initialItemSet));
        List<ItemSet> lastItemSetList;
        do {
            lastItemSetList = new ArrayList<>(itemSetList);
            for (ItemSet itemSet : itemSetList.stream().toList()) {
                grammar.getProductionSymbols().forEach(n -> addGotoItems(itemSet, n));
            }
        } while (!lastItemSetList.equals(itemSetList));
    }

    /**
     * 如果 go(itemSet,symbol) 非空且不在 itemSetList 中，则将其加入。
     *
     * @param itemSet the item set
     * @param symbol  the symbol
     */
    private void addGotoItems(ItemSet itemSet, ProductionSymbol symbol) {
        ItemSet gotoItemSet = go(itemSet, symbol);
        if (gotoItemSet.isEmpty()) {
            return;
        }
        if (itemSetList.contains(gotoItemSet)) {
            return;
        }
        itemSetList.add(gotoItemSet);
    }
}
