package com.dan323.expressions.modal;

import com.dan323.expressions.base.Negation;

public final class NegationModal extends Negation<ModalOperation> implements ModalLogicalOperation {

    public NegationModal(ModalLogicalOperation elem) {
        super(elem);
    }

    public NegationModal(ModalOperation elem) {
        this((ModalLogicalOperation) elem);
    }

    @Override
    public int hashCode() {
        return getElement().hashCode() * 5 - getClass().hashCode() * 11;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NegationModal neg){
            return neg.getElement().equals(getElement());
        } else {
            return false;
        }
    }

}
