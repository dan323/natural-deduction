package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConjunctionModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.AndI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndI<T> extends AndI<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalAndI(int i1, int i2) {
        super(i1, i2, ConjunctionModal::new);
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, get1(), get2());
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>((pf.getSteps().get(get1() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}