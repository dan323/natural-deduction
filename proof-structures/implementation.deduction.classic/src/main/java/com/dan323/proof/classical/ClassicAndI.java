package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.proof.generic.AndI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAndI extends AndI implements ClassicalAction {

    public ClassicAndI(int i1, int i2) {
        super(i1, i2, (l1, l2) -> new ConjunctionClassic((ClassicalLogicOperation) l1, (ClassicalLogicOperation) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
