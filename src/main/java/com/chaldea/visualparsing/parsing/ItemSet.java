package com.chaldea.visualparsing.parsing;

import java.util.*;

/**
 * The type Item set.项集
 */
public class ItemSet implements Iterable<Item> {
    private final Set<Item> items;

    public ItemSet(Set<Item> items) {
        this.items = items;
    }

    public ItemSet() {
        this.items = new HashSet<>();
    }

    /**
     * Add item boolean.
     *
     * @param item the item
     * @return true if this set did not already contain the specified element
     */
    public boolean addItem(Item item) {
        return items.add(item);
    }

    /**
     * Add all items.
     *
     * @param items the items
     * @return true if this set changed as a result of the call
     */
    public boolean addAllItems(Collection<Item> items) {
        return this.items.addAll(items);
    }

    /**
     * Add item set.
     *
     * @param itemSet the item set
     * @return true if this set changed as a result of the call
     */
    public boolean addAllItems(ItemSet itemSet) {
        return addAllItems(itemSet.getItemsCopy());
    }

    /**
     * Gets items copy.获取项集的副本
     *
     * @return the items copy
     */
    public Set<Item> getItemsCopy() {
        return new HashSet<>(items);
    }

    public void clear() {
        items.clear();
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Remove item boolean.
     *
     * @param item the item
     * @return true if this set contained the specified element
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * Contains boolean.
     *
     * @param item the item
     * @return true if this set contains the specified element
     */
    public boolean contains(Item item) {
        return items.contains(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemSet itemSet = (ItemSet) o;
        return Objects.equals(items, itemSet.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public Iterator<Item> iterator() {
        return items.iterator();
    }
}
