package com.dan323.expresions.modal;

import com.dan323.expresions.base.Disjunction;

public final class DisjunctionModal extends Disjunction<ModalLogicalOperation> implements ModalLogicalOperation {

    public DisjunctionModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
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