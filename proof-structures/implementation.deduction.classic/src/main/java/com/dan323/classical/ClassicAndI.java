package com.dan323.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.AndI;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAndI extends AndI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicAndI(int i1, int i2) {
        super(i1, i2, ConjunctionClassic::new);
    }

}
