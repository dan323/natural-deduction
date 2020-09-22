package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.Objects;

public final class ModalAssume<T> extends Assume<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    private final T state;

    public ModalAssume(ModalLogicalOperation clo, T st) {
        super(clo);
        state = st;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModalAssume) {
            ModalAssume<T> assume = (ModalAssume<T>) o;
            return Objects.equals(state, assume.state) &&
                    log.equals(assume.log);

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, log, getClass());
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
