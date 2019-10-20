package com.dan323.expresions.classical;

import com.dan323.expresions.base.UnaryOperation;

public abstract class UnaryOperationClassic implements ClassicalLogicOperation, UnaryOperation {

    private final ClassicalLogicOperation element;

    UnaryOperationClassic(ClassicalLogicOperation element) {
        this.element = element;
    }

    @Override
    public ClassicalLogicOperation getElement() {
        return element;
    }

    @Override
    public boolean equals(Object log) {
        return log instanceof UnaryOperationClassic && getOperator().equals(((UnaryOperationClassic) log).getOperator()) && element.equals(((UnaryOperationClassic) log).element);
    }

    @Override
    public int hashCode() {
        return element.hashCode() * 15;
    }

    @Override
    public String toString() {
        return getOperator() + " (" + getElement() + ")";
    }

    protected abstract String getOperator();
}
