package com.dan323.classical;

import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.OrE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrE extends OrE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicOrE(int a, int b, int c) {
        super(a, b, c);
    }

}
