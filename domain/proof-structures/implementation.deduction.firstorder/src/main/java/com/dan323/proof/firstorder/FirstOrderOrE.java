package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.OrE;

public final class FirstOrderOrE extends OrE<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderOrE(int a, int b, int c) {
        super(a, b, c);
    }
}
