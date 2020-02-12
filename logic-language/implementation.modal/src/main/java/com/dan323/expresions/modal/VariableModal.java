package com.dan323.expresions.modal;

import com.dan323.expresions.base.Variable;

public final class VariableModal extends Variable<ModalLogicalOperation> implements ModalLogicalOperation {
    public VariableModal(String var) {
        super(var);
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
