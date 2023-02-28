package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public class LogicalApplyAction<A extends Action<T, Q, P>, T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements ActionsUseCases.ApplyAction<A, T, Q, P> {

    @Override
    public P perform(A action, P proof) {
        if (action.isValid(proof)) {
            action.apply(proof);
        }
        return proof;
    }
}
