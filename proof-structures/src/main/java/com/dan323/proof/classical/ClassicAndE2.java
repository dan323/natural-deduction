package com.dan323.proof.classical;

import com.dan323.expresions.util.BinaryOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;

public final class ClassicAndE2 extends AndE implements ClassicalAction {

    public ClassicAndE2(int i) {
        super(i,BinaryOperation::getRight);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf,(ProofStep::new));
    }
}
