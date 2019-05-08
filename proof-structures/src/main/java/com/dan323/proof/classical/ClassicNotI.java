package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.expresions.clasical.NegationClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.NotI;

public final class ClassicNotI extends NotI implements ClassicalAction {

    public ClassicNotI() {
        super(lo -> new NegationClassic((ClassicalLogicOperation) lo));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
