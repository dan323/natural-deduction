package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.base.Constant;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.proof.modal.relational.RelationalAction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Linearity, {@code Lin [i-j]}: close the subproof that assumes {@code u <= v} (line {@code i}) and ends in
 * {@code FALSE} (line {@code j}), and derive {@code v+1 <= u}. Time is a line, so {@code u} is not after {@code v} or
 * {@code v} comes before {@code u}; the subproof ruled out the first. Reasoning by cases on the order of two states is
 * one {@code Lin} per case: assume one order, reach {@code FALSE}, and go on with the other.
 */
public final class ModalLinearity extends RelationalAction {

    /** The assumed relation of the innermost subproof, when it can be closed. */
    private static Optional<LessEqual> assumed(ModalNaturalDeduction pf) {
        int level = RuleUtils.getLastAssumptionLevel(pf);
        if (level == 0 || !(pf.getSteps().getLast().getStep() instanceof Constant constant) || !constant.isFalsehood()) {
            return Optional.empty();
        }
        var first = pf.getSteps().get(pf.getSteps().size() - RuleUtils.getToLastAssumption(pf, level));
        if (!"Ass".equals(first.getProof().getNameProof()) || !(first.getStep() instanceof LessEqual relation)) {
            return Optional.empty();
        }
        var right = StateOrder.term(relation.getRight());
        if (right.isEmpty() || !right.get().hasSuccessor() || StateOrder.term(relation.getLeft()).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(relation);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return assumed(pf).isPresent();
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var relation = assumed(pf).orElseThrow();
        int level = RuleUtils.getLastAssumptionLevel(pf);
        int length = RuleUtils.disableUntilLastAssumption(pf, level);
        var range = new ProofReason.Range(pf.getSteps().size() - length + 1, pf.getSteps().size());
        RelationOperation conclusion = new LessEqual(StateTerm.parse(relation.getRight()).successor().toString(), relation.getLeft());
        pf.getSteps().add(supp.generateProofStep(level - 1, conclusion,
                new ProofReason(ParseModalNextUntilAction.LINEARITY, List.of(range), List.of())));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalLinearity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}
