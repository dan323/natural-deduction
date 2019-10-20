package com.dan323.proof.generic;

import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStepSupplier;

/**
 * @author danco
 */
public interface AbstractAction{

    boolean isValid(Proof pf);
    void applyStepSupplier(Proof pf, ProofStepSupplier supp);

}
