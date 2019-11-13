package com.dan323.expresions.modal;

import com.dan323.expresions.base.Constant;

public class ConstantModal extends Constant<ModalLogicalOperation> implements ModalLogicalOperation {

    public final static ConstantModal TRUE = new ConstantModal(true);
    public final static ConstantModal FALSE = new ConstantModal(false);

    private final boolean val;

    private ConstantModal(boolean b) {
        val = b;
    }

    @Override
    public boolean isFalsehood() {
        return equals(FALSE);
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public String toString() {
        return String.valueOf(val).toUpperCase();
    }

}
