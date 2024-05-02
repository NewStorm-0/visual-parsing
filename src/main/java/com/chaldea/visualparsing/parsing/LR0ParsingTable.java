package com.chaldea.visualparsing.parsing;

import com.chaldea.visualparsing.exception.LRConflictException;
import com.chaldea.visualparsing.exception.LRParsingException;
import com.chaldea.visualparsing.grammar.Grammar;


/**
 * The type Lr 0 parsing table.
 * 由于LR0语法分析表的构造与SLR语法分析表的构造方法大同小异，
 * 所以继承SLRParsingTable来复用代码。按理说，应该是SLRParsingTable
 * 继承LR0ParsingTable，因为SLR是LR(0)的改进。但由于是先编写的
 * SLRParsingTable，后编写的本类，受限于时间的不足，只能以这种方
 * 式进行编写。
 */
public class LR0ParsingTable extends SLRParsingTable{

    public LR0ParsingTable(Grammar grammar) {
        super(grammar);
        changeActionTableReduce();
    }

    private void changeActionTableReduce() {
        for (ActionItem[] actionItems : actionTable) {
            ActionItem reduceItem = getUniqueReduceItem(actionItems);
            if (reduceItem == null) {
                continue;
            }
            for (int i = 0; i < actionItems.length; i++) {
                if (actionItems[i] == null) {
                    actionItems[i] = reduceItem;
                    continue;
                }
                if (actionItems[i].action() != ActionItem.Action.REDUCE) {
                    throw new LRConflictException();
                }
            }
        }
    }

    /**
     * Gets unique reduce item.
     *
     * @param actionItems the action items
     * @return the unique reduce item
     */
    private ActionItem getUniqueReduceItem(ActionItem[] actionItems) {
        ActionItem reduceItem = null;
        for (ActionItem actionItem : actionItems) {
            if (actionItem == null) {
                continue;
            }
            if (actionItem.action() != ActionItem.Action.REDUCE) {
                continue;
            }
            if (reduceItem == null) {
                reduceItem = actionItem;
                continue;
            }
            if (!reduceItem.equals(actionItem)) {
                throw new LRParsingException("不该出现本异常。LR(0)语法分析表中出现规约/规约冲突，但是这应该在SLR构造过程中就被发现");
            }
        }
        return reduceItem;
    }
}
