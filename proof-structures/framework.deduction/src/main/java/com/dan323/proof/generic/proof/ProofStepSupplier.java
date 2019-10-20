package com.dan323.proof.generic.proof;

import com.dan323.expresions.base.LogicOperation;

/**
 * @author danco
 */
@FunctionalInterface
public interface ProofStepSupplier {

    ProofStep generateProofStep(int assLevel, LogicOperation log, ProofReason reason);

}
