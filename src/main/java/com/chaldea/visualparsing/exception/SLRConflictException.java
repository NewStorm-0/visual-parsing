package com.chaldea.visualparsing.exception;

import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.ItemSet;

/**
 * The type Slr conflict exception.SLR冲突异常
 */
public class SLRConflictException extends BaseException{

    public int itemSetNumber;
    public ItemSet itemSet;
    public Terminal symbol;

    public SLRConflictException() {}

    public SLRConflictException(String message) {
        super(message);
    }

    public SLRConflictException(int number, ItemSet itemSet, Terminal symbol) {
        this.itemSetNumber = number;
        this.itemSet = itemSet;
        this.symbol = symbol;
    }

}
