package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.AndI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

public final class ModalAndI<T> extends AndI<ModalOperation, LabeledProofStep<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalAndI(int i1, int i2) {
        super(i1, i2, ConjunctionModal::new);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, get1(), get2());
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new LabeledProofStep<>(assLevel, (pf.getSteps().get(get1() - 1)).getState().get(), (ModalLogicalOperation) log, reason)));
    }
}