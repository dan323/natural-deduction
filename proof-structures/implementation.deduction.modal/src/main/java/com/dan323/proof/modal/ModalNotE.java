package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotE extends NotE<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalNotE(int j) {
        super(j);
    }

    @Override
    public boolean equals(Object ob) {
        return (ob instanceof ModalNotE) && ((ModalNotE) ob).getNeg() == getNeg();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 5;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal((pf.getSteps().get(getNeg() - 1)).getState(), assLevel, log, reason)));
    }
}
