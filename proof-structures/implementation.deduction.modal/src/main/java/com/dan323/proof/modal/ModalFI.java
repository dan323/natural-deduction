package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConstantModal;
import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.generic.FI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalFI extends FI implements ModalAction {

    private String state;

    public ModalFI(String state, int i, int j) {
        super(i, j, lo -> new NegationModal((ModalLogicalExpression) lo), () -> ConstantModal.FALSE);
        this.state = state;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (super.isValid(pf)) {
            return ModalAction.checkEqualState(pf, getPos(), getNeg());
        }
        return false;
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason));
    }
}
