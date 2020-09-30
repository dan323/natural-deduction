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

    @Override
    public boolean equals(Object log) {
        return log != null && log.getClass().equals(getClass()) && ((Variable) log).var.equals(var);
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

}
