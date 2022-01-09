package com.dan323.classical.proof;

import com.dan323.classical.internal.ClassicalAutomate;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public final class NaturalDeduction extends Proof<ClassicalLogicOperation, ClassicalLogicOperation, ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> {

    @Override
    public ClassicalLogicOperation toAssms(ProofStep<ClassicalLogicOperation> step) {
        return step.getStep();
    }

    @Override
    public ClassicalLogicOperation toGoal(ProofStep<ClassicalLogicOperation> step) {
        return step.getStep();
    }

    @Override
    public void initializeProof(List<ClassicalLogicOperation> assms, ClassicalLogicOperation goal) {
        initializeProofSteps();
        setAssms(assms);
        setGoal(goal);
    }

    @Override
    protected ProofStep<ClassicalLogicOperation> toStep(ClassicalLogicOperation logicexpression) {
        return new ProofStep<>(0, logicexpression, new ProofReason("Ass", List.of()));
    }

    public void automate() {
        ClassicalAutomate.AUTOMATIC_SOLVER.automate(this);
    }

}
