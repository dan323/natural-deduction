package com.dan323.proof.modal.proof;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.relational.RelationalAction;

import java.util.ArrayList;
import java.util.List;

public final class ModalNaturalDeduction<T> extends Proof<ModalOperation, ProofStepModal<T>> {

    private T state0;

    public static <T> boolean checkFlag(Proof<ModalOperation, ProofStepModal<T>> proof, T less, T greater) {
        for (ProofStepModal<T> pst : proof.getSteps()) {
            if (pst.isValid() && pst.getStep() instanceof LessEqual
                    && ((LessEqual<T>) pst.getStep()).getLeft().equals(less)
                    && ((LessEqual<T>) pst.getStep()).getRight().equals(greater)) {
                return true;
            }
        }
        return false;
    }

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
        for (ModalOperation lo : assms) {
            if (lo instanceof ModalLogicalOperation) {
                getSteps().add(new ProofStepModal<>(state0, 0, (ModalLogicalOperation) lo, new ProofReason("Ass", new ArrayList<>())));
            } else {
                getSteps().add(new ProofStepModal<>(0, (RelationOperation<T>) lo, new ProofReason("Ass", new ArrayList<>())));
            }
        }
    }

    @Override
    public boolean isValid(Action<ModalOperation, ProofStepModal<T>> act) {
        return (act instanceof ModalAction) || (act instanceof RelationalAction);
    }

    /**
     * Function to assert that the state {@code state} is fresh at step {@code k}:
     * does not appear anywhere in a valid statement in any step before {@code k}
     *
     * @param proof  proof to be checked
     * @param state  state checked for freshness
     * @param k      step number where {@code state} must be new
     * @param state0 initial state of the proof
     * @param <T> Type of states
     * @return true iff {@code state} does not appear in any valid proof statement of {@code proof} before {@code k}
     */
    public static <T> boolean isFresh(Proof<ModalOperation, ProofStepModal<T>> proof, T state, int k, T state0) {
        for (int i = 0; i < k - 1; i++) {
            ProofStepModal<T> step = proof.getSteps().get(i);
            if (step.isValid() && step.getStep() instanceof LessEqual) {
                LessEqual<T> operation = (LessEqual<T>) step.getStep();
                if (!operation.getLeft().equals(state) && operation.getRight().equals(state)) {
                    return false;
                }
            }

        }
        return !state.equals(state0);
    }
}