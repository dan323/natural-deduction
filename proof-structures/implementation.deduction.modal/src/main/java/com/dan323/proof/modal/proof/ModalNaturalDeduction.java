package com.dan323.proof.modal.proof;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.ModalAction;

import java.util.List;

public final class ModalNaturalDeduction<T> extends Proof<ModalOperation, ProofStepModal<T>> {

    private T state0;

    public ModalNaturalDeduction(T state0) {
        this.state0 = state0;
    }

    public T getState0() {
        return state0;
    }

    @Override
    public void initializeProof(List<ModalOperation> assms, ModalOperation goal) {
        setGoal(goal);
        initializeProofSteps();
        setAssms(assms);
    }

    @Override
    public ParseAction<ModalAction<T>, ModalNaturalDeduction<T>> getParser() {
        return ParseModalAction.getParser();
    }

    @Override
    protected ProofStepModal<T> generateAssm(ModalOperation lo) {
        if (lo instanceof ModalLogicalOperation) {
            return new ProofStepModal<>(state0, 0, (ModalLogicalOperation) lo, new ProofReason("Ass", List.of()));
        } else {
            return new ProofStepModal<>(0, (RelationOperation<T>) lo, new ProofReason("Ass", List.of()));
        }
    }

    /**
     * Function to assert that the state {@code state} is fresh at step {@code k}:
     * does not appear anywhere in a valid statement in any step before {@code k}
     *
     * @param state state checked for freshness
     * @param k     initial step number where {@code state} must not be created
     * @return true iff {@code state} is used in {@code proof} before {@code k}
     */
    public boolean stateIsUsedBefore(T state, int k) {
        boolean appears = state.equals(state0);
        for (int i = 0; i < k && !appears; i++) {
            ProofStepModal<T> step = getSteps().get(i);
            if (step.isValid()) {
                appears = appears(state, step);
            }
        }
        return appears;
    }

    private boolean appears(T state, ProofStepModal<T> step) {
        boolean appears = false;
        if (step.getStep() instanceof RelationOperation) {
            RelationOperation<T> operation = (RelationOperation<T>) step.getStep();
            if ((operation.getLeft().equals(state) || operation.getRight().equals(state)) && !(operation.getLeft().equals(state) && operation.getRight().equals(state))) {
                appears = true;
            }
        } else {
            if (step.getState().equals(state)) {
                appears = true;
            }
        }
        return appears;
    }
}