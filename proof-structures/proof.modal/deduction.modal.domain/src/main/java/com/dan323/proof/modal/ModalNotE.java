package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

public final class ModalNotE<T> extends NotE<ModalOperation, LabeledProofStep<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalNotE(int j) {
        super(j);
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new LabeledProofStep<>(assLevel, (pf.getSteps().get(getNeg() - 1)).getState().get(), (ModalLogicalOperation) log, reason)));
    }
}
