package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.OrE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrE extends OrE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> implements ClassicalAction {

    public ClassicOrE(int a, int b, int c) {
        super(a, b, c);
    }

    @Override
    public void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
