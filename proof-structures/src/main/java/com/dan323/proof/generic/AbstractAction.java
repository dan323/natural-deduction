package com.dan323.proof.generic;

import com.dan323.proof.Proof;

/**
 * @author danco
 */
public interface AbstractAction{

    boolean isValid(Proof pf);
    void applyAbstract(Proof pf, ProofStepSupplier supp);
}
