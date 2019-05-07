package com.dan323.proof.modal;

import com.dan323.proof.Proof;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalCopy extends Copy implements ModalAction {

    public ModalCopy(int i) {
        super(i);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(getState(pf, getAppliedAt()), assLevel, log, reason)));
    }

    private String getState(Proof pf, int k) {
        return ((ProofStepModal) pf.getSteps().get(k - 1)).getState();
    }
}
