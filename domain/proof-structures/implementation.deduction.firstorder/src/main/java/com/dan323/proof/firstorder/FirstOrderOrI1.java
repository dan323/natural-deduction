package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.DisjunctionFirstOrder;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.OrI;

public final class FirstOrderOrI1 extends OrI<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderOrI1(int i, FirstOrderOperation intro) {
        super(i, intro, DisjunctionFirstOrder::new);
    }
}
