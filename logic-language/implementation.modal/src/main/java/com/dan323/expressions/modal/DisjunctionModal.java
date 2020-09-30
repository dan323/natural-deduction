package com.dan323.expressions.modal;

import com.dan323.expressions.base.Disjunction;

public final class DisjunctionModal extends Disjunction<ModalOperation> implements ModalLogicalOperation {

    public DisjunctionModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
    }
    public DisjunctionModal(ModalOperation l, ModalOperation r) {
        this((ModalLogicalOperation)l, (ModalLogicalOperation)r);
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