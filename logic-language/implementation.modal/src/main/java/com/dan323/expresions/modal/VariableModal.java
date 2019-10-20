package com.dan323.expresions.modal;

import com.dan323.expresions.base.Variable;

public final class VariableModal implements ModalLogicalExpression, Variable {

    private final String var;

    public VariableModal(String var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public boolean equals(Object ltl) {
        return (ltl instanceof VariableModal) && ((VariableModal) ltl).var.equals(var);
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

}
