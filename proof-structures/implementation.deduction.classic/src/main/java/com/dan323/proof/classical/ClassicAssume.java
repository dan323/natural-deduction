package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAssume extends Assume<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> implements ClassicalAction {

    public ClassicAssume(ClassicalLogicOperation clo) {
        super(clo);
    }

}
