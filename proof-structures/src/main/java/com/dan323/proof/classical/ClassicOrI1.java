package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.expresions.clasical.DisjunctionClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.OrI;

public class ClassicOrI1 extends OrI implements ClassicalAction {

    public ClassicOrI1(int i, ClassicalLogicOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionClassic((ClassicalLogicOperation) l1, (ClassicalLogicOperation) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
