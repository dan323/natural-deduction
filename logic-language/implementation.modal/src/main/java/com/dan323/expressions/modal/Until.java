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
        return super.hashCode() * 3 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }
}
