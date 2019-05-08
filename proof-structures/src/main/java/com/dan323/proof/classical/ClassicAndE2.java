package com.dan323.proof.classical;

import com.dan323.expresions.util.BinaryOperation;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.AndE;

public final class ClassicAndE2 extends AndE implements ClassicalAction {

    public ClassicAndE2(int i) {
        super(i, BinaryOperation::getRight);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 3;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassicAndE2 && super.equals(obj);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, (ProofStep::new));
    }
}
