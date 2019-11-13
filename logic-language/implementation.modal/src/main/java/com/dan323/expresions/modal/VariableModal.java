package com.dan323.expresions.modal;

import com.dan323.expresions.base.Variable;

public final class VariableModal extends Variable implements ModalLogicalOperation {
    public VariableModal(String var) {
        super(var);
    }


    @Override
    public boolean equals(Object obj) {
        return ModalLogicalOperation.modalOperationEquals(obj, super::equals);
    }

}
