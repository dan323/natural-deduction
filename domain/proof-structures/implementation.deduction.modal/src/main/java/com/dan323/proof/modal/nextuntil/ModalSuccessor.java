package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.proof.modal.relational.RelationalAction;

import java.util.List;
import java.util.Objects;

/**
 * Successor, {@code Succ [i]}: from any formula in state {@code s}, derive {@code s <= s+1}. Like {@code Refl}, the
 * line only names the state.
 */
public final class ModalSuccessor extends RelationalAction {

    private final int line;

    public ModalSuccessor(int line) {
        this.line = line;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        if (!RuleUtils.isValidIndexAndProp(pf, line) || !(pf.getSteps().get(line - 1).getStep() instanceof ModalLogicalOperation)) {
            return false;
        }
        try {
            // s0+2147483647 is a state, but its successor cannot be written.
            return StateTerm.parse(pf.getSteps().get(line - 1).getState()).hasSuccessor();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var state = StateTerm.parse(pf.getSteps().get(line - 1).getState());
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf),
                new LessEqual(state.toString(), state.successor().toString()),
                new ProofReason(ParseModalNextUntilAction.SUCCESSOR, List.of(), List.of(line))));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalSuccessor successor && successor.line == line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line);
    }
}
