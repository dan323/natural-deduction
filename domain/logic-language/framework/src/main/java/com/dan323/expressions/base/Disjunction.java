package com.dan323.expressions.base;

/**
 * @author danco
 */
public abstract class Disjunction<T extends LogicOperation> extends BinaryOperation<T> {

    private static final String OPERATOR = "|";

    protected Disjunction(T left, T right) {
        super(left, right);
    }

    @Override
    protected String getOperator() {
        return OPERATOR;
    }
}
