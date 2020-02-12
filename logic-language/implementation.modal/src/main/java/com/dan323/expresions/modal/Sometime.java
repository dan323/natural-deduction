package com.dan323.expresions.modal;

import com.dan323.expresions.base.UnaryOperation;

public final class Sometime extends UnaryOperation<ModalOperation> implements ModalLogicalOperation {

    public Sometime(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "<>";
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
