package com.dan323.expresions.modal;

import com.dan323.expresions.base.Conjunction;

public final class ConjunctionModal extends BinaryOperationModal implements Conjunction {

    public ConjunctionModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return Conjunction.OPERATOR;
    }

}
