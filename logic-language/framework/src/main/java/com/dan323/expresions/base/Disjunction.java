package com.dan323.expresions.base;

/**
 * @author danco
 */
public abstract class Disjunction<T extends LogicOperation> extends BinaryOperation<T> {

    private static final String OPERATOR = "|";

    public Disjunction(T left, T right) {
        super(left, right);
    }

    @Override
    protected String getOperator() {
        return OPERATOR;
    }
}
