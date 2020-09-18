package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

/**
 * Class of any action taken in the proof in a classical setting
 */
public interface Action<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> extends AbstractAction<T, Q, P> {
    void apply(P pf);
}
