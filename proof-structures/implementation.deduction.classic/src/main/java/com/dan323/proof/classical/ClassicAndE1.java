package com.dan323.proof.classical;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAndE1 extends AndE implements ClassicalAction {

    public ClassicAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 2;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassicAndE1 && super.equals(obj);
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, (ProofStep::new));
    }
}