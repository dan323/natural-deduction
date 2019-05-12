package com.dan323.expresions.modal;

import com.dan323.expresions.util.Negation;

public final class NegationModal extends UnaryOperationModal implements Negation {

    public NegationModal(ModalLogicalExpression elem) {
        super(elem);
    }

    @Override
    protected String getOperator() {
        return "-";
    }

}
