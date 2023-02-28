package com.dan323.proof.generic.proof;

import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
@FunctionalInterface
public interface ProofStepSupplier<T extends LogicOperation, Q extends ProofStep<T>> {

    Q generateProofStep(int assLevel, T log, ProofReason reason);

}
