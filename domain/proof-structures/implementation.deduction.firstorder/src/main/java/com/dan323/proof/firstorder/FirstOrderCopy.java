package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.Copy;

import java.util.Objects;

public final class FirstOrderCopy extends Copy<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderCopy(int i) {
        super(i);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppliedAt(), getClass());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderCopy copy && getAppliedAt() == copy.getAppliedAt();
    }
}
