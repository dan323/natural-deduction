package com.dan323.expressions.modal;

public final class Always extends UnaryModal {

    public Always(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "[]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Always alw) {
            return alw.getElement().equals(getElement());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getElement().hashCode() * 7 + getClass().hashCode() * 13;
    }
}