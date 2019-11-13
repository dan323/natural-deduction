package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrI1 extends OrI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> implements ClassicalAction {

    public ClassicOrI1(int i, ClassicalLogicOperation intro) {
        super(i, intro, DisjunctionClassic::new);
    }

    @Override
    public void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
