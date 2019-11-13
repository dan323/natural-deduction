package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public interface AbstractAction<T extends LogicOperation, Q extends ProofStep<T>> {

    boolean isValid(Proof<T, Q> pf);

    void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T,Q> supp);

}
