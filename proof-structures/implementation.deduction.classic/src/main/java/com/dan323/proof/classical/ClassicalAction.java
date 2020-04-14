package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.ProofStep;

public interface ClassicalAction extends Action<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> {

    @Override
    default void apply(NaturalDeduction pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
