package com.dan323.expresions.modal;

import com.dan323.expresions.base.Constant;

public final class ConstantModal implements ModalLogicalExpression, Constant {

    public static final ConstantModal TRUE = new ConstantModal(true);
    public static final ConstantModal FALSE = new ConstantModal(false);
    private final boolean val;

    private ConstantModal(boolean b) {
        val = b;
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }

    @Override
    public boolean equals(Object ltl) {
        return (ltl instanceof ConstantModal) && ((ConstantModal) ltl).val == val;
    }

    @Override
    public int hashCode() {
        return val ? 1 : 0;
    }

    @Override
    public Constant construct(double val) {
        if (val != 0.0 && val != 1.0) {
            throw new IllegalArgumentException();
        }
        return val == 1.0 ? TRUE : FALSE;
    }
}
