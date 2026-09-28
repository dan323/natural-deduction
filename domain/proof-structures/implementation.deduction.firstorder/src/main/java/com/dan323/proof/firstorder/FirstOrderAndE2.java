package com.dan323.proof.firstorder;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.AndE;

public final class FirstOrderAndE2 extends AndE<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderAndE2(int i) {
        super(i, BinaryOperation::getRight);
    }
}
