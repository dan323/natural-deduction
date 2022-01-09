package com.dan323.proof.modal;

import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

public final class ModalOrI1<T> extends OrI<ModalOperation, LabeledProofStep<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalOrI1(int i, ModalLogicalOperation intro) {
        super(i, intro, DisjunctionModal::new);
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new LabeledProofStep<>(assLevel, (pf.getSteps().get(getAt() - 1)).getState().get(), (ModalLogicalOperation) log, reason)));
    }
}
