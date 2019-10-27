package com.dan323.expresions.modal;

import com.dan323.expresions.base.Disjunction;

public final class DisjunctionModal extends BinaryOperationModal implements Disjunction {

    public DisjunctionModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return Disjunction.OPERATOR;
    }

}