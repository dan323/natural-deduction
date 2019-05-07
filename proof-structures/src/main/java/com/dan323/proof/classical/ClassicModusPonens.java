package com.dan323.proof.classical;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.ModusPonens;

public class ClassicModusPonens extends ModusPonens implements ClassicalAction {

    public ClassicModusPonens(int a, int b) {
        super(a, b);
    }


    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
