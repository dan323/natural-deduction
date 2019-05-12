package com.dan323.expresions.modal;

import com.dan323.expresions.util.Conjuntion;

public final class ConjuntionModal extends BinaryOperationModal implements Conjuntion {

    public ConjuntionModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "&";
    }

}
