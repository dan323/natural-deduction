package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConjuntionModal;
import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.AndI;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAndI extends AndI implements ModalAction {

    public ModalAndI(int i1, int i2) {
        super(i1, i2, (l1, l2) -> new ConjuntionModal((ModalLogicalExpression) l1, (ModalLogicalExpression) l2));
    }

    @Override
    public boolean isValid(Proof pf) {
        return ModalAction.checkEqualState(pf, get1(), get2()) &&
                super.isValid(pf);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(((ProofStepModal) pf.getSteps().get(get1() - 1)).getState(), assLevel, log, reason)));
    }
}