package com.dan323.expressions.modal;

public final class Sometime extends UnaryModal {

    public Sometime(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "<>";
    }
}
