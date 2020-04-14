package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAssume<T> extends Assume<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    private final T state;

    public ModalAssume(ModalLogicalOperation clo, T st) {
        super(clo);
        state = st;
    }

    public ModalAssume(RelationOperation<T> operation) {
        super(operation);
        state = null;
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        if (state == null) {
            applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(assLevel, (RelationOperation<T>) log, reason));
        } else {
            applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(state, assLevel, (ModalLogicalOperation) log, reason));
        }
    }
}
