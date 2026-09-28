package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.ImplicationFirstOrder;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.DeductionTheorem;

public final class FirstOrderDeductionTheorem extends DeductionTheorem<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderDeductionTheorem() {
        super(ImplicationFirstOrder::new);
    }
}
