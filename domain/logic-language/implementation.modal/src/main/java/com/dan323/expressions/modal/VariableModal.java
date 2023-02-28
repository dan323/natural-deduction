package com.dan323.expressions.modal;

import com.dan323.expressions.base.Variable;

public final class VariableModal extends Variable implements ModalLogicalOperation {
    public VariableModal(String var) {
        super(var);
    }

    @Override
    public int hashCode() {
        return toString().hashCode() * 3 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableModal varMod){
            return varMod.toString().equals(toString());
        } else {
            return false;
        }
    }

}
