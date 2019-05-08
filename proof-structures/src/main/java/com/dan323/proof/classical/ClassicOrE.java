package com.dan323.proof.classical;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.OrE;

public final class ClassicOrE extends OrE implements ClassicalAction {

    public ClassicOrE(int a, int b, int c) {
        super(a, b, c);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
