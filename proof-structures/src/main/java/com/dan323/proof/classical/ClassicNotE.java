package com.dan323.proof.classical;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.NotE;

public final class ClassicNotE extends NotE implements ClassicalAction {

    public ClassicNotE(int j) {
        super(j);
    }

    @Override
    public boolean equals(Object ob) {
        return (ob instanceof ClassicNotE) && ((ClassicNotE) ob).getNeg() == getNeg();
    }

    @Override
    public int hashCode() {
        return getNeg() * 31;
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
