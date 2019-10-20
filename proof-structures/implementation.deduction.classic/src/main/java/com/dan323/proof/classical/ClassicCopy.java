package com.dan323.proof.classical;

import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicCopy extends Copy implements ClassicalAction {

    public ClassicCopy(int i) {
        super(i);
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
