package com.dan323.proof.modal;

import com.dan323.proof.Proof;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalModusPonens extends ModusPonens implements ModalAction {

    public ModalModusPonens(int i1, int i2) {
        super(i1, i2);
    }

    @Override
    public boolean isValid(Proof pf) {
        return ModalAction.checkEqualState(pf, get1(), get2()) && super.isValid(pf);
    }

    @Override
    public void apply(Proof pf) {
        ProofStepModal imp = (ProofStepModal) pf.getSteps().get(get1() - 1);
        String st = imp.getState();
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(st, assLevel, log, reason)));
    }
}
