package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.proof.ProofReason;
import com.dan323.proof.ProofStep;

/**
 * @author danco
 */
@FunctionalInterface
public interface ProofStepSupplier {

    ProofStep generateProofStep(int assLevel, LogicOperation log, ProofReason reason);

}
