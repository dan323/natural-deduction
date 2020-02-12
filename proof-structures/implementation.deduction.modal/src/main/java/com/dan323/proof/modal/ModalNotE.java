package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotE<T> extends NotE<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalNotE(int j) {
        super(j);
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>((pf.getSteps().get(getNeg() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
