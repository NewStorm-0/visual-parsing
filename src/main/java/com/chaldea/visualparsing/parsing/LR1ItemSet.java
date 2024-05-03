package com.chaldea.visualparsing.parsing;

import java.util.*;

/**
 * 该类为编写中被遗弃的类，该类的原意为其中的元素为LR(1)项的缩写，例如 C→·cC, c/d。
 * 即包含的LR(1)项可以有多个向前看符号。你不应该使用该类，如果你有兴趣，可以完善该类，
 * 并且编写一个新的LR1Items类或修改LR1Item类，现有的LR1Item类只有一个向前看符号。
 */
@Deprecated
public class LR1ItemSet extends ItemSet{

    private LR1ItemSet() {
        super();
    }

    private LR1ItemSet(Set<LR1Item> lr1Items) {
        super(new HashSet<>(lr1Items));
    }

    @Override
    public boolean addItem(Item item) {
        return addItem((LR1Item) item);
    }

    public boolean addItem(LR1Item lr1Item) {
        for (Item item : this) {
            LR1Item lr1ItemOfItems = (LR1Item) item;
            if (!lr1ItemOfItems.itemEquals(lr1Item)) {
                continue;
            }
            super.removeItem(lr1ItemOfItems);
//            LR1Item newLR1Item = new LR1Item()
        }
        return super.addItem(lr1Item);
    }

    @Override
    public boolean addAllItems(Collection<Item> items) {
        items.stream().map(n -> (LR1Item) n).forEach(this::addItem);
        // Avoid compiler error
        return false;
    }

    @Override
    public boolean addAllItems(ItemSet itemSet) {
        return super.addAllItems(itemSet);
    }

    @Override
    public Set<Item> getItemsCopy() {
        return super.getItemsCopy();
    }
}
