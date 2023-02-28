package com.dan323.classical;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAndE1 extends AndE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }
}
