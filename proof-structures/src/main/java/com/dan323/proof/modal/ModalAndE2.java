package com.dan323.proof.modal;

import com.dan323.expresions.util.BinaryOperation;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndE2 extends AndE implements ModalAction {

    public ModalAndE2(int i) {
        super(i, BinaryOperation::getRight);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(getState(pf), assLevel, log, reason)));
    }

    private String getState(Proof pf) {
        return ((ProofStepModal) pf.getSteps().get(getAppliedAt() - 1)).getState();
    }
}
