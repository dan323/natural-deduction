package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.relation.LessEqual;
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
 * Until witness, {@code UW [i]}: from {@code A U B} in state {@code s} (line {@code i}), derive {@code s <= t} for a
 * new state {@code t}, the state where {@code B} is reached. {@code t} names that state for the rest of the proof:
 * {@code UB} gives {@code B} in it and {@code UA} gives {@code A} in every state from {@code s} to just before it.
 * <p>
 * {@code t} must be a base that no valid line uses (as the new state of {@code []I} and {@code <>E}), so it names this
 * witness and nothing else; like those states, it cannot be generalized over later, because {@code []I} and
 * {@code <>E} only introduce unused states.
 */
public final class ModalUntilWitness extends RelationalAction {

    private final int line;
    private final String state;

    /**
     * @param line  the line with {@code A U B}
     * @param state the new state, or null to take one no line uses
     */
    public ModalUntilWitness(int line, String state) {
        this.line = line;
        this.state = state;
    }

    private Optional<LessEqual> conclusion(ModalNaturalDeduction pf) {
        if (!RuleUtils.isValidIndexAndProp(pf, line) || !(pf.getSteps().get(line - 1).getStep() instanceof Until)) {
            return Optional.empty();
        }
        var from = pf.getSteps().get(line - 1).getState();
        var witness = state == null ? pf.newState() : state;
        if (!pf.isFreshState(witness, from, pf.getSteps().size())) {
            return Optional.empty();
        }
        return Optional.of(new LessEqual(from, witness));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return conclusion(pf).isPresent();
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), conclusion(pf).orElseThrow(),
                new ProofReason(ParseModalNextUntilAction.UNTIL_WITNESS, List.of(), List.of(line))));
    }

    /**
     * @return the {@code A U B} that the {@code UW} step at {@code line} (1-based) names a witness of, with the state it
     * holds in, when that line is a valid {@code UW} step
     */
    public static Optional<Witness> witness(ModalNaturalDeduction pf, int line) {
        if (!RuleUtils.isValidIndexAndProp(pf, line)) {
            return Optional.empty();
        }
        var step = pf.getSteps().get(line - 1);
        var reason = step.getProof();
        if (!ParseModalNextUntilAction.UNTIL_WITNESS.equals(reason.getNameProof()) || !(step.getStep() instanceof LessEqual relation)) {
            return Optional.empty();
        }
        int source = Integer.parseInt(reason.toString().substring(reason.getNameProof().length() + 2, reason.toString().length() - 1).trim());
        if (!RuleUtils.isValidIndexAndProp(pf, source) || !(pf.getSteps().get(source - 1).getStep() instanceof Until until)) {
            return Optional.empty();
        }
        return Optional.of(new Witness(until, relation.getLeft(), relation.getRight()));
    }

    /**
     * @param until the formula {@code A U B}
     * @param from  the state {@code s} it holds in
     * @param state the witness {@code t}
     */
    public record Witness(Until until, String from, String state) {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalUntilWitness other && other.line == line && Objects.equals(other.state, state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, state);
    }
}
