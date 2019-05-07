package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.expresions.clasical.ConjuntionClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.AndI;

public final class ClassicAndI extends AndI implements ClassicalAction {

    public ClassicAndI(int i1, int i2) {
        super(i1, i2, (l1, l2) -> new ConjuntionClassic((ClassicalLogicOperation) l1, (ClassicalLogicOperation) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
