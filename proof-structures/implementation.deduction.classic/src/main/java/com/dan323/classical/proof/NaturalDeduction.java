package com.dan323.classical.proof;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.internal.ClassicalAutomate;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public final class NaturalDeduction extends Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> {

    @Override
    public void setGoal(ClassicalLogicOperation g) {
        super.setGoal(g);
    }

    @Override
    public void initializeProof(List<ClassicalLogicOperation> assms, ClassicalLogicOperation goal) {
        initializeProofSteps();
        setAssms(assms);
        setGoal(goal);
    }

    @Override
    public ParseAction<ClassicalAction, NaturalDeduction> getParser() {
        return ParseClassicalAction.PARSE_CLASSICAL_ACTION;
    }

    @Override
    protected ProofStep<ClassicalLogicOperation> generateAssm(ClassicalLogicOperation logicExpresion) {
        return new ProofStep<>(0, logicExpresion, new ProofReason("Ass", List.of()));
    }

    public void automate() {
        ClassicalAutomate.AUTOMATIC_SOLVER.automate(this);
    }

}
