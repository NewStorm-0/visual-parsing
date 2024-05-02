package com.chaldea.visualparsing.parsing;

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

    public static String toString(ActionItem actionItem) {
        if (actionItem == null) {
            return "";
        }
        if (actionItem.action() == ActionItem.Action.ACCEPT) {
            return "acc";
        }
        if (actionItem.action() == ActionItem.Action.REDUCE) {
            return "r" + actionItem.number();
        }
        if (actionItem.action() == ActionItem.Action.SHIFT) {
            return "s" + actionItem.number();
        }
        return String.valueOf(actionItem.number());
    }
}
