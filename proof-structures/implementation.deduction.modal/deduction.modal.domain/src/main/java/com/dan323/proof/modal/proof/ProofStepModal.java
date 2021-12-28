package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

public class ProofStepModal extends ProofStep<ModalOperation> {

    public ProofStepModal(int ass, ModalOperation log, ProofReason proofReason) {
        super(ass, log, proofReason);
    }

    @Override
    public String toString() {
        return getState() + ": " + super.toString();
    }

    public String getState() {
        if (getStep() instanceof ModalLogicalOperation log) {
            return log.getLabel();
        } else {
            return ModalLogicalOperation.S0;
        }
    }
}
