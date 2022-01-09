package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;

import java.util.List;
import java.util.Optional;

public final class ModalNaturalDeduction<T> extends Proof<ModalOperation, LabeledExpression<T>, LabeledExpression<T>, LabeledProofStep<T>> {

    @Override
    public LabeledExpression<T> toAssms(LabeledProofStep<T> step) {
        if (step.getStep() instanceof ModalLogicalOperation lo && step.getState().isPresent()){
            return new LabeledExpression<>(step.getState().get(), lo);
        } else {
            return new LabeledExpression<>((RelationOperation<T>) step.getStep());
        }
    }

    @Override
    public LabeledExpression<T> toGoal(LabeledProofStep<T> step) {
        if (step.getStep() instanceof ModalLogicalOperation mod){
            return new LabeledExpression<>(step.getState().get(), mod);
        } else {
            return new LabeledExpression<>((RelationOperation<T>) step.getStep());
        }
    }

    @Override
    protected LabeledProofStep<T> toStep(LabeledExpression<T> logicexpression) {
        if (logicexpression.getExpression() instanceof ModalLogicalOperation loMLO) {
            return new LabeledProofStep<>(0, logicexpression.getLabel(), loMLO, new ProofReason("Ass", List.of()));
        } else {
            return new LabeledProofStep<>(0, (RelationOperation<T>) logicexpression.getExpression(), new ProofReason("Ass", List.of()));
        }
    }

    @Override
    public void initializeProof(List<LabeledExpression<T>> assms, LabeledExpression<T> goal) {
        setGoal(goal);
        initializeProofSteps();
        setAssms(assms);
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
        boolean appears = false;
        for (int i = 0; i < k && !appears; i++) {
            LabeledProofStep<T> step = getSteps().get(i);
            if (step.isValid()) {
                appears = appears(state, step);
            }
        }
        return appears;
    }

    private boolean appears(T state, LabeledProofStep<T> step) {
        boolean appears = false;
        if (step.getStep() instanceof RelationOperation) {
            RelationOperation<T> operation = (RelationOperation<T>) step.getStep();
            if ((operation.getLeft().equals(Optional.of(state)) || operation.getRight().equals(Optional.of(state))) && !(operation.getLeft().equals(Optional.of(state)) && operation.getRight().equals(Optional.of(state)))) {
                appears = true;
            }
        } else {
            if (step.getState().equals(Optional.of(state))) {
                appears = true;
            }
        }
        return appears;
    }
}