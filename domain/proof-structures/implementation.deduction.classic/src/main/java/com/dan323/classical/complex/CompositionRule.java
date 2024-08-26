package com.dan323.classical.complex;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public abstract class CompositionRule implements ClassicalAction {

    @Override
    public void applyStepSupplier(NaturalDeduction pf, ProofStepSupplier<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> supp) {
        throw new UnsupportedOperationException("This rule is a composition, hence no abstraction");
    }

    @Override
    public AvailableAction getAction() {
        throw new IllegalStateException("This is not a basic action");
    }
}
