package com.dan323.proof.modal.proof;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

public class ProofStepModal<T> extends ProofStep<ModalOperation> {

    private final T state;

    public ProofStepModal(T state, int ass, ModalLogicalOperation log, ProofReason proofReason) {
        super(ass, log, proofReason);
        this.state = state;
    }

    public ProofStepModal(int ass, RelationOperation<T> log, ProofReason proofReason) {
        super(ass, log, proofReason);
        state = null;
    }

    @Override
    public String toString() {
        if (getStep() instanceof RelationOperation) {
            return super.toString();
        } else {
            return getState() + ": " + super.toString();
        }
    }

    public T getState() {
        return state;
    }
}
