package com.dan323.proof.modal;

import com.dan323.expresions.modal.DisjunctionModal;
import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrI1 extends OrI implements ModalAction {

    public ModalOrI1(int i, ModalLogicalExpression intro) {
        super(i, intro, (l1, l2) -> new DisjunctionModal((ModalLogicalExpression) l1, (ModalLogicalExpression) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal(((ProofStepModal) pf.getSteps().get(getAt() - 1)).getState(), assLevel, log, reason)));
    }
}
