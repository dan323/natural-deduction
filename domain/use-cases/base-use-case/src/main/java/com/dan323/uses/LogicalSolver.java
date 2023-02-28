package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public interface LogicalSolver<T extends LogicOperation,Q extends ProofStep<T>,P extends Proof<T,Q>> extends ActionsUseCases.Solve<T,Q,P> {
    String getLogicName();
}
