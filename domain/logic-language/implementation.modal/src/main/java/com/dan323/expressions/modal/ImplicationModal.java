package com.dan323.expressions.modal;

import com.dan323.expressions.base.Implication;

public final class ImplicationModal extends Implication<ModalOperation> implements ModalLogicalOperation {

    public ImplicationModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
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
        return getRight().hashCode() * 3 - 7 * getLeft().hashCode() + 11 * getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImplicationModal imp) {
            return imp.getLeft().equals(getLeft()) && imp.getRight().equals(getRight());
        } else {
            return false;
        }
    }

}
