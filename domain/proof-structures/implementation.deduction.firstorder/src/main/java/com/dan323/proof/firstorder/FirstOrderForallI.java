package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code ∀I}: from line {@code i}, {@code A[x:=a]}, derive the target {@code forall x. A}. The name {@code a} must be
 * arbitrary: free in no premise, in no open assumption, and not in the target. When {@code x} is not free in {@code A},
 * line {@code i} must be {@code A} itself.
 */
public final class FirstOrderForallI implements FirstOrderAction {

    public static final String NAME = "∀I";

    private final int line;
    private final Forall target;

    public FirstOrderForallI(int line, Forall target) {
        this.line = line;
        this.target = target;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        if (target == null || !RuleUtils.isValidIndexAndProp(pf, line)) {
            return false;
        }
        var match = Instances.instanceOf(target.getBody(), target.getVariable(), pf.getSteps().get(line - 1).getStep());
        if (!match.matches()) {
            return false;
        }
        if (match.isVacuous()) {
            return true;
        }
        Optional<String> name = Instances.name(match.term());
        return name.isPresent()
                && !target.freeVariables().contains(name.get())
                && !pf.isFreeInOpenAssumption(name.get(), pf.getSteps().size());
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), target,
                new ProofReason(NAME, List.of(), List.of(line))));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderForallI other && line == other.line && Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, target);
    }
}
