package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrI2 extends OrI implements ClassicalAction {

    public ClassicOrI2(int i, ClassicalLogicOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionClassic((ClassicalLogicOperation) l2, (ClassicalLogicOperation) l1));
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}