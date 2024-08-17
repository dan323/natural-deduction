package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public interface ProofParser<P extends Proof<T,Q>, T extends LogicOperation, Q extends ProofStep<T>> {

    P parseProof(String proof);

    String logic();

}
