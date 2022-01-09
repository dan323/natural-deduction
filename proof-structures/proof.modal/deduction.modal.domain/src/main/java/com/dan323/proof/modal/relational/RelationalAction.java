package com.dan323.proof.modal.relational;

import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.LabeledProofStep;

public abstract class RelationalAction<T> implements AbstractModalAction<T> {

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new LabeledProofStep<>(assLevel, (RelationOperation<T>) log, reason));
    }
}
