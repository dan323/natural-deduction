package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.Assume;

import java.util.Objects;

public final class FirstOrderAssume extends Assume<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> implements FirstOrderAction {

    public FirstOrderAssume(FirstOrderOperation formula) {
        super(formula);
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        return log != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), log);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderAssume ass && Objects.equals(log, ass.log);
    }
}
