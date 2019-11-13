package com.dan323.proof.modal.proof;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

public class ProofStepModal extends ProofStep<ModalLogicalOperation> {

    private String state;

    public ProofStepModal(String state, int ass, ModalLogicalOperation statement, ProofReason why) {
        super(ass, statement, why);
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return getState() + ": " + super.toString();
    }
}
