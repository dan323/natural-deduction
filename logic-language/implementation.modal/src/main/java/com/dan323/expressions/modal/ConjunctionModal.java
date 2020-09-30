package com.dan323.expressions.modal;

import com.dan323.expressions.base.Conjunction;

public final class ConjunctionModal extends Conjunction<ModalOperation> implements ModalLogicalOperation {

    public ConjunctionModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
    }

    public ConjunctionModal(ModalOperation l, ModalOperation r) {
        this((ModalLogicalOperation) l, (ModalLogicalOperation) r);
    }

    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 3 + getClass().hashCode();
    }
}
