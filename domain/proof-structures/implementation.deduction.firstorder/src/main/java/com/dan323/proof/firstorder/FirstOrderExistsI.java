package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.Exists;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * {@code ∃I}: from line {@code i}, {@code A[x:=t]} for some term {@code t}, derive the target {@code exists x. A}.
 */
public final class FirstOrderExistsI implements FirstOrderAction {

    public static final String NAME = "∃I";

    private final int line;
    private final Exists target;

    public FirstOrderExistsI(int line, Exists target) {
        this.line = line;
        this.target = target;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        return target != null && RuleUtils.isValidIndexAndProp(pf, line)
                && Instances.instanceOf(target.getBody(), target.getVariable(), pf.getSteps().get(line - 1).getStep()).matches();
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), target,
                new ProofReason(NAME, List.of(), List.of(line))));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderExistsI other && line == other.line && Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, target);
    }
}
