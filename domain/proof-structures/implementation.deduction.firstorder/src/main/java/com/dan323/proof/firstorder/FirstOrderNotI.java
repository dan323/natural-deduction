package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.NegationFirstOrder;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.NotI;

public final class FirstOrderNotI extends NotI<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderNotI() {
        super(NegationFirstOrder::new);
    }
}
