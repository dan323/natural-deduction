package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.Equals;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * {@code =E}: from line {@code i}, {@code s = t}, and line {@code j}, {@code A}, derive the target: {@code A} with some
 * of the free occurrences of {@code s} replaced by {@code t}, where no quantifier of {@code A} captures a name of
 * {@code t}.
 */
public final class FirstOrderEqualsE implements FirstOrderAction {

    public static final String NAME = "=E";

    private final int equation;
    private final int line;
    private final FirstOrderOperation target;

    public FirstOrderEqualsE(int equation, int line, FirstOrderOperation target) {
        this.equation = equation;
        this.line = line;
        this.target = target;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        if (target == null
                || !RuleUtils.isValidIndexAndProp(pf, equation)
                || !RuleUtils.isValidIndexAndProp(pf, line)
                || !RuleUtils.isOperation(pf, equation, Equals.class)) {
            return false;
        }
        Equals eq = (Equals) pf.getSteps().get(equation - 1).getStep();
        return Instances.isReplacement(pf.getSteps().get(line - 1).getStep(), target, eq.left(), eq.right());
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), target,
                new ProofReason(NAME, List.of(), List.of(equation, line))));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderEqualsE other && equation == other.equation && line == other.line
                && Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), equation, line, target);
    }
}
