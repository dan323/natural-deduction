package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public interface ClassicalAction extends Action<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> {

    @Override
    default void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
