package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrI2 extends OrI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicOrI2(int i, ClassicalLogicOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionClassic(l2, l1));
    }
}
