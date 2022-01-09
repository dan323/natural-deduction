package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

public final class ModalModusPonens<T> extends ModusPonens<ModalOperation, LabeledProofStep<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalModusPonens(int i1, int i2) {
        super(i1, i2);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, get1(), get2());
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        LabeledProofStep<T> imp = pf.getSteps().get(get1() - 1);
        T st = imp.getState().get();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new LabeledProofStep<>(assLevel, st, (ModalLogicalOperation) log, reason)));
    }
}
