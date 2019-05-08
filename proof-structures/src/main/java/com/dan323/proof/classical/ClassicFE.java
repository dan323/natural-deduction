package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.FE;

public final class ClassicFE extends FE implements ClassicalAction {

    public ClassicFE(int j, ClassicalLogicOperation clo) {
        super(clo, j);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
