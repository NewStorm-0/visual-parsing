package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.grammar.Grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LRCollection implements Iterable<ItemSet> {

    protected final Grammar grammar;
    protected final List<ItemSet> itemSetList;

    public LRCollection(Grammar grammar) {
        this.grammar = grammar;
        itemSetList = new ArrayList<>();
    }

    /**
     * the size of itemSetList.项集的数目
     *
     * @return the int
     */
    public int size() {
        return itemSetList.size();
    }

    /**
     * Get item set.
     *
     * @param index the index
     * @return the item set
     */
    public ItemSet get(int index) {
        return itemSetList.get(index);
    }

    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Gets item set number.
     *
     * @param itemSet the item set
     * @return the item set number
     */
    public int getItemSetNumber(ItemSet itemSet) {
        return itemSetList.indexOf(itemSet);
    }

    @Override
    public Iterator<ItemSet> iterator() {
        return itemSetList.iterator();
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
