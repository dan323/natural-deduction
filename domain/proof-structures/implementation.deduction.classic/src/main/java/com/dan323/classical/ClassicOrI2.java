package com.dan323.classical;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.DisjunctionClassic;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicOrI2 extends OrI<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicOrI2(int i, ClassicalLogicOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionClassic(l2, l1));
    }

    @Override
    public AvailableAction getAction() {
        return AvailableAction.ORI2;
    }
}
