package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.FE;

public final class FirstOrderFE extends FE<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderFE(int j, FirstOrderOperation intro) {
        super(intro, j);
    }
}
