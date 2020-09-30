package com.dan323.classical;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.ModusPonens;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicModusPonens extends ModusPonens<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicModusPonens(int a, int b) {
        super(a, b);
    }

}
