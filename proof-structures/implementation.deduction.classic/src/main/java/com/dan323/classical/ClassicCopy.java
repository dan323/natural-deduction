package com.dan323.classical;

import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicCopy extends Copy<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicCopy(int i) {
        super(i);
    }

}
