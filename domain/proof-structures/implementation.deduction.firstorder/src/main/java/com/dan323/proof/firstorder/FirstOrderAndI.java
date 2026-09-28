package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.ConjunctionFirstOrder;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.AndI;

public final class FirstOrderAndI extends AndI<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderAndI(int i1, int i2) {
        super(i1, i2, ConjunctionFirstOrder::new);
    }
}
