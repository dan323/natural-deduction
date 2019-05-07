package com.dan323.proof.classical;

import com.dan323.expresions.util.BinaryOperation;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;

public final class ClassicAndE1 extends AndE implements ClassicalAction {

    public ClassicAndE1(int i) {
        super(i,BinaryOperation::getLeft);
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, (ProofStep::new));
    }
}
