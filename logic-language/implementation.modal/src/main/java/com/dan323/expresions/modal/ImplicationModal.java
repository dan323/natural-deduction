package com.dan323.expresions.modal;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;

public final class ImplicationModal extends Implication<ModalLogicalOperation> implements ModalLogicalOperation {

    public ImplicationModal(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
    }

    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }

}
