package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAssume extends Assume implements ClassicalAction {

    public ClassicAssume(ClassicalLogicOperation clo){
        super(clo);
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
