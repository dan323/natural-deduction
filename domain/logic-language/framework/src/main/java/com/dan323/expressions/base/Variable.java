package com.dan323.expressions.base;

/**
 * @author danco
 */
public abstract class Variable implements LogicOperation {

    private final String name;

    protected Variable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
