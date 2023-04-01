package com.dan323.proof.modal;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndE1 extends AndE<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(getState(pf), assLevel, (ModalLogicalOperation) log, reason));
    }

    private String getState(ModalNaturalDeduction pf) {
        return (pf.getSteps().get(getAppliedAt() - 1)).getState();
    }
}
