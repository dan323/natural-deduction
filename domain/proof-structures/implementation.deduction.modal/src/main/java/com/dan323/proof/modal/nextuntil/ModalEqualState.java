package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Equal states, {@code Eq [i, j, ...]}: from {@code A} in state {@code u} (line {@code i}), derive {@code A} in state
 * {@code v} when the relations on lines {@code j, ...} give {@code u <= v} and {@code v <= u}: then {@code u} and
 * {@code v} are the same state. A client names {@code v}, and the relation lines are the ones it needs.
 */
public final class ModalEqualState implements ModalAction {

    private final int line;
    private final String state;
    private final List<Integer> relations;
    private final LastSupport lastSupport = new LastSupport();

    /** The rule as a client applies it: the relation lines are found among the valid relations. */
    public ModalEqualState(int line, String state) {
        this(line, state, null);
    }

    /**
     * @param line      the line with the formula
     * @param state     the state to derive it in
     * @param relations the relation lines to reason with, or null to find them
     */
    public ModalEqualState(int line, String state, List<Integer> relations) {
        this.line = line;
        this.state = state;
        this.relations = relations == null ? null : List.copyOf(relations);
    }

    private Optional<SortedSet<Integer>> support(ModalNaturalDeduction pf) {
        if (state == null || !RuleUtils.isValidIndexAndProp(pf, line)
                || !(pf.getSteps().get(line - 1).getStep() instanceof ModalLogicalOperation)) {
            return Optional.empty();
        }
        var from = pf.getSteps().get(line - 1).getState();
        var used = Relations.order(pf, relations).flatMap(order -> order.sameState(from, state));
        return relations == null ? used : used.map(found -> new TreeSet<>(relations));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return lastSupport.find(pf, this::support).isPresent();
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var cited = new ArrayList<Integer>();
        cited.add(line);
        cited.addAll(lastSupport.take(pf, this::support).orElseThrow());
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), pf.getSteps().get(line - 1).getStep(),
                new ProofReason(ParseModalNextUntilAction.EQUAL, List.of(), cited)));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalEqualState other && other.line == line && Objects.equals(other.state, state)
                && Objects.equals(other.relations, relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, state, relations);
    }
}
