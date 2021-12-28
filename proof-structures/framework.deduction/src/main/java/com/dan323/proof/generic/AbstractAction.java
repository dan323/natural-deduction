package com.dan323.proof.generic;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public interface AbstractAction<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> {

    boolean isValid(P pf);

    void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp);

}
