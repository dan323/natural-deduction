package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicNotI extends NotI implements ClassicalAction {

    public ClassicNotI() {
        super(lo -> new NegationClassic((ClassicalLogicOperation) lo));
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
