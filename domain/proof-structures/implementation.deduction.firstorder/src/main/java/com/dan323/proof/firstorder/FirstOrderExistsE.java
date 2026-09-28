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
import java.util.Optional;

/**
 * {@code ∃E}: line {@code i} is {@code exists x. A}, and the innermost open subproof opens with the assumption
 * {@code A[x:=a]} and closes on {@code C}. It discharges the subproof (as {@code <>E} does in modal logic) and derives
 * {@code C} one level up. The name {@code a} must be fresh: not in the {@code ∃} formula, not in {@code C}, and free in
 * no premise and no assumption open outside the subproof. Line {@code i} must be outside the subproof.
 */
public final class FirstOrderExistsE implements FirstOrderAction {

    public static final String NAME = "∃E";

    private final int line;

    public FirstOrderExistsE(int line) {
        this.line = line;
    }

    @Override
    public boolean isValid(FirstOrderNaturalDeduction pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        // 0-based index of the assumption that opens the subproof
        int start = pf.getSteps().size() - RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStep<FirstOrderOperation> assumption = pf.getSteps().get(start);
        if (!"Ass".equals(assumption.getProof().getNameProof())
                || line > start
                || !RuleUtils.isValidIndexAndProp(pf, line)
                || !RuleUtils.isOperation(pf, line, Exists.class)) {
            return false;
        }
        Exists exists = (Exists) pf.getSteps().get(line - 1).getStep();
        var match = Instances.instanceOf(exists.getBody(), exists.getVariable(), assumption.getStep());
        if (!match.matches()) {
            return false;
        }
        if (match.isVacuous()) {
            return true;
        }
        Optional<String> name = Instances.name(match.term());
        return name.isPresent()
                && !exists.freeVariables().contains(name.get())
                && !pf.getSteps().getLast().getStep().freeVariables().contains(name.get())
                && !pf.isFreeInOpenAssumption(name.get(), start);
    }

    @Override
    public void applyStepSupplier(FirstOrderNaturalDeduction pf, ProofStepSupplier<FirstOrderOperation, ProofStep<FirstOrderOperation>> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        FirstOrderOperation conclusion = pf.getSteps().getLast().getStep();
        int length = RuleUtils.disableUntilLastAssumption(pf, assLevel);
        var range = new ProofReason.Range(pf.getSteps().size() - length + 1, pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, conclusion, new ProofReason(NAME, List.of(range), List.of(line))));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirstOrderExistsE other && line == other.line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line);
    }
}
