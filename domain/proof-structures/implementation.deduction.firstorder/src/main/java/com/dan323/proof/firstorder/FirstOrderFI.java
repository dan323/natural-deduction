package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.ConstantFirstOrder;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.FI;

public final class FirstOrderFI extends FI<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderFI(int i, int j) {
        super(i, j, () -> ConstantFirstOrder.FALSE);
    }
}
