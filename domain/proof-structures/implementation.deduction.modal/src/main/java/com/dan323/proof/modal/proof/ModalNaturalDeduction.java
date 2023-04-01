package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.modal.AbstractModalAction;

import java.util.List;

public final class ModalNaturalDeduction extends Proof<ModalOperation, ProofStepModal> {

    private final String state0;

    public ModalNaturalDeduction(String state0) {
        this.state0 = state0;
    }

    public String getState0() {
        return state0;
    }

    @Override
    public void initializeProof(List<ModalOperation> assms, ModalOperation goal) {
        setGoal(goal);
        initializeProofSteps();
        setAssms(assms);
    }

    @Override
    public List<AbstractModalAction> parse() {
        return ((ParseAction<AbstractModalAction, ModalNaturalDeduction, ModalOperation, ProofStepModal>) ParseModalAction::parse).translateToActions(this);
    }

    @Override
    protected ProofStepModal generateAssm(ModalOperation lo) {
        if (lo instanceof ModalLogicalOperation) {
            return new ProofStepModal(state0, 0, (ModalLogicalOperation) lo, new ProofReason("Ass", List.of()));
        } else {
            return new ProofStepModal(0, (RelationOperation) lo, new ProofReason("Ass", List.of()));
        }
    }

    @Override
    public void automate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Function to assert that the state {@code state} is fresh at step {@code k}:
     * does not appear anywhere in a valid statement in any step before {@code k}
     *
     * @param state state checked for freshness
     * @param k     initial step number where {@code state} must not be created
     * @return true iff {@code state} is used in {@code proof} before {@code k}
     */
    public boolean stateIsUsedBefore(String state, int k) {
        boolean appears = state.equals(state0);
        for (int i = 0; i < k && !appears; i++) {
            ProofStepModal step = getSteps().get(i);
            if (step.isValid()) {
                appears = appears(state, step);
            }
        }
        return appears;
    }

    private boolean appears(String state, ProofStepModal step) {
        boolean appears = false;
        if (step.getStep() instanceof RelationOperation operation) {
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

    public String newState() {
        var lst = getSteps().stream()
                .filter(ProofStep::isValid)
                .map(ProofStepModal::getState).toList();
        int i = lst.size();
        while (lst.contains("s" + i)) {
            i++;
        }
        return "s" + i;
    }
}