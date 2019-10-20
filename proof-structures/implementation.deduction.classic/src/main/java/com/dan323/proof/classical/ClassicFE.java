package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.FE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicFE extends FE implements ClassicalAction {

    public ClassicFE(int j, ClassicalLogicOperation clo) {
        super(clo, j);
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
