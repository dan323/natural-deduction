package com.dan323.proof.classical.complex;

import com.dan323.proof.classical.ClassicalAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public abstract class CompositionRule implements ClassicalAction {

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        throw new UnsupportedOperationException("This rule is a composition, hence no abstraction");
    }
}
