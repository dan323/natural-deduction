package com.dan323.expressions.modal;

public final class Sometime extends UnaryModal {

    public Sometime(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "<>";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Sometime some) {
            return some.getElement().equals(getElement());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getElement().hashCode() * 7 + getClass().hashCode() * 13;
    }
}
