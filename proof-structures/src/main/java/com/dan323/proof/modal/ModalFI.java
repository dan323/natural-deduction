package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConstantModal;
import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.FI;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalFI extends FI implements ModalAction {

    public ModalFI(int i, int j) {
        super(i, j, lo -> new NegationModal((ModalLogicalExpression) lo), () -> new ConstantModal(false));
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
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(((ProofStepModal) pf.getSteps().get(getPos() - 1)).getState(), assLevel, log, reason)));
    }
}
