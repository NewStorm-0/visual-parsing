package com.chaldea.visualparsing.parsing;

import org.controlsfx.control.action.Action;

/**
 * The type Action item.
 *
 * <p>Action为相应动作，number为项集或表达式编号</p>
 */
public record ActionItem(Action action, int number) {
    /**
     * The enum Action.
     */
    public enum Action {
        /**
         * 移入
         */
        SHIFT,
        /**
         * 规约
         */
        REDUCE,
        /**
         * 接受
         */
        ACCEPT;
    }
}
