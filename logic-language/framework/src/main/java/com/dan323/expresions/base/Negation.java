package com.dan323.expresions.base;

/**
 * @author danco
 */
public abstract class Negation<T extends LogicOperation> extends UnaryOperation<T> {

    private static final String OPERATOR = "-";

    public Negation(T element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return OPERATOR;
    }
}
