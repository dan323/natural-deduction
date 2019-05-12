package com.dan323.expresions.modal;

import com.dan323.expresions.util.Implication;

public final class ImplicationModal extends BinaryOperationModal implements Implication {


    public ImplicationModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }


    @Override
    protected String getOperator() {
        return "->";
    }

}
