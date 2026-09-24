package com.dan323.expressions.modal;

/**
 * Next, written {@code X A}: {@code X A} holds in state {@code s} when {@code A} holds in the successor state
 * {@code s+1} (see {@link com.dan323.expressions.relation.StateTerm}). Only the {@code modal-next-until} logic reads it.
 */
public final class Next extends UnaryModal {

    public Next(ModalLogicalOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "X";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Next next) {
            return next.getElement().equals(getElement());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getElement().hashCode() * 17 + getClass().hashCode() * 23;
    }
}
