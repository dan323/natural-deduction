package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.expressions.firstorder.Term;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * {@code ∀E}: from line {@code i}, {@code forall x. A}, and a term {@code t}, derive {@code A[x:=t]} (a
 * capture-avoiding substitution).
 */
public final class FirstOrderForallE implements FirstOrderAction {

    public static final String NAME = "∀E";

    private final int line;
    private final Term term;

    public FirstOrderForallE(int line, Term term) {
        this.line = line;
        this.term = term;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        return term != null && RuleUtils.isValidIndexAndProp(pf, line) && RuleUtils.isOperation(pf, line, Forall.class);
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        Forall forall = (Forall) pf.getSteps().get(line - 1).getStep();
        FirstOrderOperation result = forall.getBody().substitute(forall.getVariable(), term);
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), result,
                new ProofReason(NAME, List.of(), List.of(line))));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderForallE other && line == other.line && Objects.equals(term, other.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, term);
    }
}
