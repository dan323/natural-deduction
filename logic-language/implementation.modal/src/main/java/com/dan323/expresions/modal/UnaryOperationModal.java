package com.dan323.expresions.modal;

import com.dan323.expresions.base.UnaryOperation;

public abstract class UnaryOperationModal implements ModalLogicalExpression, UnaryOperation {

    private final ModalLogicalExpression element;

    public UnaryOperationModal(ModalLogicalExpression element) {
        this.element = element;
    }

    public ModalLogicalExpression getElement() {
        return element;
    }


    public String toString() {
        return getOperator() + " (" + getElement() + ")";
    }

    protected abstract String getOperator();

    @Override
    public final boolean equals(Object log) {
        return
                log instanceof UnaryOperationModal && // The object is of the appropriate class
                        getOperator().equals(((UnaryOperationModal) log).getOperator()) && // The operations is the same
                        element.equals(((UnaryOperationModal) log).element); // The expression over which we apply it, is the same
    }

    @Override
    public final int hashCode() {
        return element.hashCode() * 13 - 11 * getOperator().hashCode();
    }
}
