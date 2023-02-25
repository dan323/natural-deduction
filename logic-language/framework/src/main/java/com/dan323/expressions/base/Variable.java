package com.dan323.expressions.base;

/**
 * @author danco
 */
public abstract class Variable implements LogicOperation {

    private final String var;

    public Variable(String var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return var;
    }

}
