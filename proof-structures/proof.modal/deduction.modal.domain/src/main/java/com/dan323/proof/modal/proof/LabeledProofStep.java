package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.Optional;

public class LabeledProofStep<T> extends ProofStep<ModalOperation> {

    private final T label;

    public LabeledProofStep(int ass, T label, ModalLogicalOperation expression, ProofReason proofReason) {
        super(ass, expression, proofReason);
        this.label = label;
    }

    public LabeledProofStep(int ass, RelationOperation<T> expression, ProofReason proofReason) {
        super(ass, expression, proofReason);
        this.label = null;
    }

    @Override
    public String toString() {
        if (getState().isPresent()){
            return getState().get() + ": " + super.toString();
        } else {
            return "N: " + super.toString();
        }
    }

    public Optional<T> getState() {
        return Optional.of(getStep())
                .filter(ModalLogicalOperation.class::isInstance)
                .map(exp -> label);
    }
}
