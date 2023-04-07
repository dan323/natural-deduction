package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.Objects;

public final class ModalAssume extends Assume<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    private final String state;

    public ModalAssume(ModalLogicalOperation clo, String st) {
        super(clo);
        state = st;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModalAssume assume) {
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

    public ModalAssume(RelationOperation operation) {
        super(operation);
        state = null;
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        if (state == null) {
            applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(assLevel, (RelationOperation) log, reason));
        } else {
            applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason));
        }
    }
}
