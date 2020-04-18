package com.dan323.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicNotI extends NotI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicNotI() {
        super(NegationClassic::new);
    }

}
