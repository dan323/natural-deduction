package com.dan323.expresions.modal;

public final class Sometime extends UnaryOperationModal {

    public Sometime(ModalLogicalExpression element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "<>";
    }
}
