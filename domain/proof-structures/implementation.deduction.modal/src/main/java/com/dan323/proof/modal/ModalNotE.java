package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotE extends NotE<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalNotE(int j) {
        super(j);
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal((pf.getSteps().get(getNeg() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
