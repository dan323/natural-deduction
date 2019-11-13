package com.dan323.proof.modal;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndE1 extends AndE<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(getState(pf), assLevel, log, reason)));
    }

    private String getState(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        return (pf.getSteps().get(getAppliedAt() - 1)).getState();
    }
}
