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
        return getRight().hashCode() * 3 - getLeft().hashCode() *13 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DisjunctionModal disj){
            return disj.getLeft().equals(getLeft()) && disj.getRight().equals(getRight());
        } else {
            return false;
        }
    }
}