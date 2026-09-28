package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.Equals;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Term;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * {@code =I}: for a term {@code t}, derive {@code t = t}.
 */
public final class FirstOrderEqualsI implements FirstOrderAction {

    public static final String NAME = "=I";

    private final Term term;

    public FirstOrderEqualsI(Term term) {
        this.term = term;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        return term != null;
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), new Equals(term, term),
                new ProofReason(NAME, List.of(), List.of())));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderEqualsI other && Objects.equals(term, other.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), term);
    }
}
