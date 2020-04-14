package com.dan323.proof.modal.relational;

import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public abstract class RelationalAction<T> implements Action<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> {

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(assLevel, (RelationOperation<T>) log, reason));
    }
}
