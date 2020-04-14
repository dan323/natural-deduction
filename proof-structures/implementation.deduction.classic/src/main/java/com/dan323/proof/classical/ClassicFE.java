package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.FE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicFE extends FE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicFE(int j, ClassicalLogicOperation clo) {
        super(clo, j);
    }

}
