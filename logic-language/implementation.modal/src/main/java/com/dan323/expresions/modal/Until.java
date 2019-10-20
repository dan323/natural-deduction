package com.dan323.expresions.modal;

public final class Until extends BinaryOperationModal {

    public Until(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "U";
    }
}
