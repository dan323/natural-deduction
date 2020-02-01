package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConstantModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.generic.FI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalFI extends FI<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    private String state;

    public ModalFI(String state, int i, int j) {
        super(i, j, NegationModal::new, () -> ConstantModal.FALSE);
        this.state = state;
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        if (super.isValid(pf)) {
            return ModalAction.checkEqualState(pf, getPos(), getNeg());
        }
        return false;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason));
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 7 + state.hashCode() * 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModalFI) {
            return super.equals(obj) && ((ModalFI) obj).state.equals(state);
        } else {
            return false;
        }
    }
}
