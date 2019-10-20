package com.dan323.proof.modal.proof;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

public class ProofStepModal extends ProofStep {

    private String state;

    public ProofStepModal(String state, int ass, LogicOperation statement, ProofReason why) {
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
