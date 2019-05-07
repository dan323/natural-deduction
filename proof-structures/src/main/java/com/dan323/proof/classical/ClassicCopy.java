package com.dan323.proof.classical;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.Copy;

public final class ClassicCopy extends Copy implements ClassicalAction {

    public ClassicCopy(int i) {
        super(i);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
