package com.dan323.expressions.modal;

import com.dan323.expressions.base.UnaryOperation;

public abstract class UnaryModal extends UnaryOperation<ModalLogicalOperation> implements ModalLogicalOperation {

    public UnaryModal(ModalLogicalOperation element) {
        super(element);
    }

}
