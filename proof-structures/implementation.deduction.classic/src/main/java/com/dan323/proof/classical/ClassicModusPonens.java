package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicModusPonens extends ModusPonens<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> implements ClassicalAction {

    public ClassicModusPonens(int a, int b) {
        super(a, b);
    }


    @Override
    public void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
