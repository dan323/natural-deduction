package com.dan323.proof.firstorder.proof;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.FirstOrderAction;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

/**
 * A proof of first-order logic with equality. Its steps are plain {@link ProofStep}s, printed in the usual text layout.
 * It has no automatic solver.
 */
public final class FirstOrderNaturalDeduction extends Proof<FirstOrderOperation, ProofStep<FirstOrderOperation>> {

    private static final String ASSUMPTION = "Ass";

    @Override
    public void initializeProof(List<FirstOrderOperation> assms, FirstOrderOperation goal) {
        initializeProofSteps();
        setAssms(assms);
        setGoal(goal);
    }

    @Override
    public List<FirstOrderAction> parse() {
        return ((ParseAction<FirstOrderAction, FirstOrderNaturalDeduction, FirstOrderOperation, ProofStep<FirstOrderOperation>>) ParseFirstOrderAction::parse).translateToActions(this);
    }

    @Override
    protected ProofStep<FirstOrderOperation> generateAssm(FirstOrderOperation logicExpression) {
        return new ProofStep<>(0, logicExpression, new ProofReason(ASSUMPTION, List.of(), List.of()));
    }

    /**
     * First-order logic has no automatic solver.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void automate() {
        throw new UnsupportedOperationException("First-order logic has no automatic solver");
    }

    /**
     * @param name   a name
     * @param before the number of steps to look at, from the first
     * @return whether {@code name} is free in a premise or an open assumption among the first {@code before} steps (a
     * discharged assumption is disabled, so it does not count)
     */
    public boolean isFreeInOpenAssumption(String name, int before) {
        return getSteps().subList(0, before).stream()
                .filter(ProofStep::isValid)
                .filter(step -> ASSUMPTION.equals(step.getProof().getNameProof()))
                .anyMatch(step -> step.getStep().freeVariables().contains(name));
    }
}
