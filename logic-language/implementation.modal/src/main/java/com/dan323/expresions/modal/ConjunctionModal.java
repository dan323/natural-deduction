package com.dan323.expresions.modal;

import com.dan323.expresions.base.Conjunction;

public final class ConjunctionModal extends Conjunction<ModalLogicalOperation> implements ModalLogicalOperation {

    public ConjunctionModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
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
