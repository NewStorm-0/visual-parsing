package com.chaldea.visualparsing.grammar;

/**
 * 终结符
 */
public class Terminal extends ProductionSymbol{

    public static final Terminal EMPTY_STRING = new Terminal("ε");
    public static final Terminal END_MARKER = new Terminal("#");
    public Terminal(String value) {
        super(value);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
