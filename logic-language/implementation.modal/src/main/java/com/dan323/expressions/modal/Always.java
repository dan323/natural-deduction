package com.dan323.expressions.modal;

public final class Always extends UnaryModal {

    public Always(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "[]";
    }
}