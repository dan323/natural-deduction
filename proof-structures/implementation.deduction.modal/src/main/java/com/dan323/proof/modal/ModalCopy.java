package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalCopy extends Copy<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalCopy(int i) {
        super(i);
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(getState(pf, getAppliedAt()), assLevel, log, reason)));
    }

    private String getState(Proof<ModalLogicalOperation, ProofStepModal> pf, int k) {
        return (pf.getSteps().get(k - 1)).getState();
    }
}
