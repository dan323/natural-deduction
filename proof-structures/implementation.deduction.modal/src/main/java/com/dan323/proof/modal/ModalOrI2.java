package com.dan323.proof.modal;

import com.dan323.expresions.modal.DisjunctionModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrI2 extends OrI<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalOrI2(int i, ModalLogicalOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionModal(l2, l1));
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal(pf.getSteps().get(getAt() - 1).getState(), assLevel, log, reason)));
    }
}
