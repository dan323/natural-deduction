package com.dan323.expresions.base;

/**
 * @author danco
 */
public abstract class Conjunction<T extends LogicOperation> extends BinaryOperation<T> {

    private static final String OPERATOR = "&";

    public Conjunction(T left, T right) {
        super(left, right);
    }

    @Override
    protected String getOperator() {
        return OPERATOR;
    }
}
