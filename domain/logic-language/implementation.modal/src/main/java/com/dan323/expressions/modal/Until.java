package com.dan323.expressions.modal;

import com.dan323.expressions.base.BinaryOperation;

public final class Until extends BinaryOperation<ModalOperation> implements ModalLogicalOperation {

    public Until(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "U";
    }

    @Override
    public int hashCode() {
        return getRight().hashCode() * 3 - 6 * getLeft().hashCode() + 31 * getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Until unt) {
            return unt.getLeft().equals(getLeft()) && unt.getRight().equals(getRight());
        } else {
            return false;
        }
    }
}
