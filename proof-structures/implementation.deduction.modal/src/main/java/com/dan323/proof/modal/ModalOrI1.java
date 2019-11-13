package com.dan323.proof.modal;

import com.dan323.expresions.modal.DisjunctionModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrI1 extends OrI<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalOrI1(int i, ModalLogicalOperation intro) {
        super(i, intro, DisjunctionModal::new);
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal((pf.getSteps().get(getAt() - 1)).getState(), assLevel, log, reason)));
    }
}
