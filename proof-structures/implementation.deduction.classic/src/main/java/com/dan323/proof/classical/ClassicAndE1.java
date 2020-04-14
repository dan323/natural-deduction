package com.dan323.proof.classical;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.AndE;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicAndE1 extends AndE<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicAndE1(int i) {
        super(i, BinaryOperation::getLeft);
    }
}
