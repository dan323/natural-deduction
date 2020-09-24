package com.dan323.proof.modal.relational;

import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public abstract class RelationalAction<T> implements AbstractModalAction<T> {

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(assLevel, (RelationOperation<T>) log, reason));
    }
}
