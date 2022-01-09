package com.dan323.proof.modal;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

import java.util.Optional;

public final class ModalAndE1<T> extends AndE<ModalOperation, LabeledProofStep<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new LabeledProofStep<>(assLevel, getState(pf).get(), (ModalLogicalOperation) log, reason)));
    }

    private Optional<T> getState(ModalNaturalDeduction<T> pf) {
        return (pf.getSteps().get(getAppliedAt() - 1)).getState();
    }
}
