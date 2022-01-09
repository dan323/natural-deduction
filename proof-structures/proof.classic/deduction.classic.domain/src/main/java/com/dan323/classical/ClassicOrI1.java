package com.dan323.classical;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.DisjunctionClassic;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrI1 extends OrI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicOrI1(int i, ClassicalLogicOperation intro) {
        super(i, intro, DisjunctionClassic::new);
    }

}
