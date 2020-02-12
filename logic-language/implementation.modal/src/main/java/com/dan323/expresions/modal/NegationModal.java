package com.dan323.expresions.modal;

import com.dan323.expresions.base.Negation;

public final class NegationModal extends Negation<ModalOperation> implements ModalLogicalOperation {

    public NegationModal(ModalLogicalOperation elem) {
        super(elem);
    }

    public NegationModal(ModalOperation elem) {
        this((ModalLogicalOperation) elem);
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
