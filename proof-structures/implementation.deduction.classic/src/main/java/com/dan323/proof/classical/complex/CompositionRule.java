package com.dan323.proof.classical.complex;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.ClassicalAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public abstract class CompositionRule implements ClassicalAction {

    @Override
    public void applyStepSupplier(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf, ProofStepSupplier<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> supp) {
        throw new UnsupportedOperationException("This rule is a composition, hence no abstraction");
    }
}
