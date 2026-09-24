package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public interface Transformer<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>, A extends Action<T, Q, P>> {

    String logic();

    P from(ProofDto proofDto);

    A from(ActionDto actionDto);

    ProofDto fromProof(P proof);

    /**
     * Whether {@link Proof#automate()} solves proofs of this logic. A logic that shares the proofs of another one, but
     * not all of its rules, answers false: the other logic's solver could use a rule this logic does not have.
     */
    default boolean hasSolver() {
        return true;
    }
}
