package com.dan323.expresions.modal;

public final class Always extends UnaryOperationModal {

    public Always(ModalLogicalExpression element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "[]";
    }

}
