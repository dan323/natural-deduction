package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalModusPonens extends ModusPonens<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalModusPonens(int i1, int i2) {
        super(i1, i2);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 5;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModalModusPonens) && super.equals(obj);
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        return ModalAction.checkEqualState(pf, get1(), get2()) && super.isValid(pf);
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        ProofStepModal imp = pf.getSteps().get(get1() - 1);
        String st = imp.getState();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(st, assLevel, log, reason)));
    }
}
