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

    /**
     * Instantiates a new Lr conflict exception.
     *
     * @param state         状态，即行号索引
     * @param itemSet       状态对应的项集
     * @param symbol        列的符号
     * @param oldActionItem 已经存在的动作
     */
    public LRConflictException(int state, ItemSet itemSet, Terminal symbol,
                               ActionItem oldActionItem) {
        super("在状态 " + state + " 上，终结符 " + symbol.getValue() + " 处产生冲突。原有动作为 "
                + ActionItem.toString(oldActionItem));
        this.state = state;
        this.itemSet = itemSet;
        this.symbol = symbol;
        this.oldActionItem = oldActionItem;
    }

    /**
     * Instantiates a new Lr conflict exception.
     *
     * @param state         状态，即行号索引
     * @param itemSet       状态对应的项集
     * @param symbol        列的符号
     * @param oldActionItem 已经存在的动作
     * @param newActionItem 新的冲突的动作
     */
    public LRConflictException(int state, ItemSet itemSet, Terminal symbol,
                               ActionItem oldActionItem, ActionItem newActionItem) {
        super("在状态 " + state + " 上，终结符 " + symbol.getValue() + " 处产生冲突。原有动作为 "
                + ActionItem.toString(oldActionItem) + " ，新动作为 " + ActionItem.toString(newActionItem));
        this.state = state;
        this.itemSet = itemSet;
        this.symbol = symbol;
        this.oldActionItem = oldActionItem;
        this.newActionItem = newActionItem;
    }

}
