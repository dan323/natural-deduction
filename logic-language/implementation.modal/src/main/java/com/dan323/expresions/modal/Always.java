package com.dan323.expresions.modal;

import com.dan323.expresions.base.UnaryOperation;

public final class Always extends UnaryOperation<ModalOperation> implements ModalLogicalOperation {

    public Always(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "[]";
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 5 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }

}
