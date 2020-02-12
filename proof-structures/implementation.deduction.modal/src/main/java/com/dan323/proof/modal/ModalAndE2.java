package com.dan323.proof.modal;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndE2<T> extends AndE<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalAndE2(int i) {
        super(i, BinaryOperation::getRight);
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(getState(pf), assLevel, (ModalLogicalOperation) log, reason)));
    }

    private T getState(Proof<ModalOperation, ProofStepModal<T>> pf) {
        return (pf.getSteps().get(getAppliedAt() - 1)).getState();
    }
}
