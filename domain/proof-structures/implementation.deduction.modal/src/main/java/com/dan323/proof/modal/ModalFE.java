package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.FE;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalFE extends FE<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    private final String state;

    public ModalFE(int j, ModalLogicalOperation clo, String st) {
        super(clo, j);
        state = st;
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason)));
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ((ModalFE) o).state.equals(state);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 7 + state.hashCode() * 5;
    }
}
