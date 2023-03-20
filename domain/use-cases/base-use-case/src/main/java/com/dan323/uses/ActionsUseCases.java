package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public interface ActionsUseCases {

    GetActions getActions(String logicName);

    <A extends Action<T, Q, P>, T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> ApplyAction<A, T, Q, P> applyAction(String logicName);

    <T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> Solve<T, Q, P> solveProblem(String logicName);

    interface GetActions {
        List<String> perform();
    }

    interface ApplyAction<A extends Action<T, Q, P>, T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> {
        P perform(A action, P proof);
    }

    interface Solve<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> {
        P perform(P proof);
    }
}
