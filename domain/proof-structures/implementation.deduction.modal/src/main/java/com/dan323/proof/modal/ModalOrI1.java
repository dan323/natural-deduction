package com.dan323.proof.modal;

import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrI1 extends OrI<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalOrI1(int i, ModalLogicalOperation intro) {
        super(i, intro, (a,b) -> new DisjunctionModal((ModalLogicalOperation) a,(ModalLogicalOperation) b));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal((pf.getSteps().get(getAt() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
