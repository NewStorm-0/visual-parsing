package com.chaldea.visualparsing.exception;

import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.ActionItem;
import com.chaldea.visualparsing.parsing.ItemSet;


/**
 * The type Lr conflict exception.
 */
public class LRConflictException extends BaseException{

    /**
     * 状态
     */
    public int state;
    public ItemSet itemSet;
    public Terminal symbol;
    public ActionItem oldActionItem, newActionItem;

    public LRConflictException() {}

    public LRConflictException(String message) {
        super(message);
    }

    public LRConflictException(int number, ItemSet itemSet, Terminal symbol,
                               ActionItem oldActionItem) {
        super("在状态 " + number + " 上，终结符 " + symbol.getValue() + " 处产生冲突。原有动作为 "
                + ActionItem.toString(oldActionItem));
        this.state = number;
        this.itemSet = itemSet;
        this.symbol = symbol;
        this.oldActionItem = oldActionItem;
    }

    public LRConflictException(int number, ItemSet itemSet, Terminal symbol,
                               ActionItem oldActionItem, ActionItem newActionItem) {
        super("在状态 " + number + " 上，终结符 " + symbol.getValue() + " 处产生冲突。原有动作为 "
                + ActionItem.toString(oldActionItem) + " ，新动作为 " + ActionItem.toString(newActionItem));
        this.state = number;
        this.itemSet = itemSet;
        this.symbol = symbol;
        this.oldActionItem = oldActionItem;
        this.newActionItem = newActionItem;
    }

}
