package com.dan323.classical;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicNotE extends NotE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicNotE(int j) {
        super(j);
    }

    @Override
    public AvailableAction getAction() {
        return AvailableAction.NOTE;
    }
}
