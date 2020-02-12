package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalCopy<T> extends Copy<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalCopy(int i) {
        super(i);
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(getState(pf, getAppliedAt()), assLevel, (ModalLogicalOperation) log, reason));
    }

    private T getState(Proof<ModalOperation, ProofStepModal<T>> pf, int k) {
        return (pf.getSteps().get(k - 1)).getState();
    }
}
