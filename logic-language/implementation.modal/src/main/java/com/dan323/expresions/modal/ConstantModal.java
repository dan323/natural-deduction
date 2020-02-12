package com.dan323.expresions.modal;

import com.dan323.expresions.base.Constant;

public final class ConstantModal implements ModalLogicalOperation, Constant<ModalOperation> {

    public static final ConstantModal TRUE = new ConstantModal(true);
    public static final ConstantModal FALSE = new ConstantModal(false);

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

    @Override
    public int hashCode() {
        return super.hashCode() * 3 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstantModal && ((ConstantModal) obj).val == val;
    }

}
