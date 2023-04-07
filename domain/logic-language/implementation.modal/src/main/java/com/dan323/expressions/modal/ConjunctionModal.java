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
        if (obj instanceof ConjunctionModal conj) {
            return conj.getRight().equals(getRight()) && conj.getLeft().equals(getLeft());
        } else {
            return false;
        }
    }

    @Override
    public ModalLogicalOperation getLeft() {
        return (ModalLogicalOperation) super.getLeft();
    }

    @Override
    public ModalLogicalOperation getRight() {
        return (ModalLogicalOperation) super.getRight();
    }

    @Override
    public int hashCode() {
        return getLeft().hashCode() * 3 + getRight().hashCode() * 7 + getClass().hashCode();
    }
}
