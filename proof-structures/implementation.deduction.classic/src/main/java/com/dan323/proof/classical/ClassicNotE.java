package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.NotE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicNotE extends NotE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicNotE(int j) {
        super(j);
    }

}
