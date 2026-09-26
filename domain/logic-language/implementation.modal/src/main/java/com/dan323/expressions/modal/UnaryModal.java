package com.dan323.expressions.modal;

import com.dan323.expressions.base.UnaryOperation;

public abstract class UnaryModal extends UnaryOperation<ModalLogicalOperation> implements ModalLogicalOperation {

    protected UnaryModal(ModalLogicalOperation element) {
        super(element);
    }

}
