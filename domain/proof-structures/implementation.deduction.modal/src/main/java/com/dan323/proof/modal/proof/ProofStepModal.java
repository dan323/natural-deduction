package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

public class ProofStepModal extends ProofStep<ModalOperation> {

    private final String state;

    public ProofStepModal(String state, int ass, ModalLogicalOperation log, ProofReason proofReason) {
        super(ass, log, proofReason);
        this.state = state;
    }

    public ProofStepModal(int ass, RelationOperation log, ProofReason proofReason) {
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

    public String getState() {
        return state;
    }
}
