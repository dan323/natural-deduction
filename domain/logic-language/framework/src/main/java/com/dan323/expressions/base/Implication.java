package com.dan323.expressions.base;

/**
 * @author danco
 */
public abstract class Implication<T extends LogicOperation> extends BinaryOperation<T> {

    private static final String OPERATOR = "->";

    public Implication(T left, T right) {
        super(left, right);
    }

    @Override
    protected String getOperator() {
        return OPERATOR;
    }
}
