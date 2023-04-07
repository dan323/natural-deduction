package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalModusPonens extends ModusPonens<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalModusPonens(int i1, int i2) {
        super(i1, i2);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, get1(), get2());
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        ProofStepModal imp = pf.getSteps().get(get1() - 1);
        String st = imp.getState();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(st, assLevel, (ModalLogicalOperation) log, reason)));
    }
}
