package com.dan323.expresions.modal;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.LogicOperation;

public final class Until extends BinaryOperation<ModalLogicalOperation> implements ModalLogicalOperation {

    public Until(ModalLogicalOperation l, ModalLogicalOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "U";
    }

    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }
}
