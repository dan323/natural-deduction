package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicCopy extends Copy<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> implements ClassicalAction {

    public ClassicCopy(int i) {
        super(i);
    }

}
