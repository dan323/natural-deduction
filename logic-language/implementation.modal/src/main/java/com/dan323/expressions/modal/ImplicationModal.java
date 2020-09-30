package com.dan323.expressions.modal;

import com.dan323.expressions.base.Implication;

public final class ImplicationModal extends Implication<ModalOperation> implements ModalLogicalOperation {

    public ImplicationModal(ModalOperation l, ModalOperation r) {
        this((ModalLogicalOperation) l, (ModalLogicalOperation) r);
    }

    public ImplicationModal(ModalLogicalOperation l, ModalLogicalOperation r) {
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
