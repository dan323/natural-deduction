package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;
import java.util.Optional;

/**
 * A rule of {@code modal-next-until} that derives a formula from earlier lines without closing an assumption. It is
 * valid when {@link #conclusion} finds what to derive, and adds that formula at the current assumption level.
 */
abstract sealed class NextUntilRule implements ModalAction
        permits ModalNextI, ModalNextE, ModalUntilI1, ModalUntilI2, ModalUntilE, ModalUntilSometime, ModalInduction {

    private final String name;
    private final List<Integer> lines;

    NextUntilRule(String name, List<Integer> lines) {
        this.name = name;
        this.lines = List.copyOf(lines);
    }

    /** What the rule derives, in which state, or empty when it does not apply. Only called on valid lines. */
    abstract Optional<Conclusion> conclusion(ModalNaturalDeduction pf);

    record Conclusion(ModalLogicalOperation formula, String state) {
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return lines.stream().allMatch(line -> RuleUtils.isValidIndexAndProp(pf, line)) && conclusion(pf).isPresent();
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var conclusion = conclusion(pf).orElseThrow();
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), conclusion.formula(),
                new ProofReason(name, List.of(), lines)));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        var state = conclusion(pf).orElseThrow().state();
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason));
    }

    /** The formula at line {@code line} (1-based), empty for a relation. */
    static Optional<ModalLogicalOperation> formula(ModalNaturalDeduction pf, int line) {
        return pf.getSteps().get(line - 1).getStep() instanceof ModalLogicalOperation formula ? Optional.of(formula) : Optional.empty();
    }

    /** The formula at line {@code line} if it is a {@code type}. */
    static <F extends ModalLogicalOperation> Optional<F> formula(ModalNaturalDeduction pf, int line, Class<F> type) {
        return formula(pf, line).filter(type::isInstance).map(type::cast);
    }

    static String state(ModalNaturalDeduction pf, int line) {
        return pf.getSteps().get(line - 1).getState();
    }

    /** The state of line {@code line} as a term, empty when it is not one. */
    static Optional<StateTerm> term(ModalNaturalDeduction pf, int line) {
        try {
            return Optional.of(StateTerm.parse(state(pf, line)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass()) && ((NextUntilRule) obj).lines.equals(lines);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 31 + lines.hashCode();
    }
}
