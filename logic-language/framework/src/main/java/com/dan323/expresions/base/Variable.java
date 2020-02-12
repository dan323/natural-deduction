package com.dan323.expresions.base;

/**
 * @author danco
 */
public abstract class Variable<T> implements LogicOperation {

    private final String var;

    public T castToLanguage() {
        return (T) this;
    }

    public Variable(String var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public boolean equals(Object log) {
        return log != null && log.getClass().equals(getClass()) && ((Variable<?>) log).var.equals(var);
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

}
