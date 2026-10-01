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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Until holds before its witness, {@code UA [i, j, ...]}: from the {@code UW} step {@code s <= t} (line {@code i}) of
 * {@code A U B}, derive {@code A} in a state {@code k} when the relations on lines {@code j, ...} give
 * {@code s <= k} and {@code k+1 <= t}, i.e. {@code k} is from {@code s} on and before {@code t}. A client names
 * {@code k}, and the relation lines are the ones it needs.
 */
public final class ModalUntilWitnessLeft implements ModalAction {

    private final int line;
    private final String state;
    private final List<Integer> relations;
    private final LastSupport lastSupport = new LastSupport();

    /** The rule as a client applies it: the relation lines are found among the valid relations. */
    public ModalUntilWitnessLeft(int line, String state) {
        this(line, state, null);
    }

    /**
     * @param line      the {@code UW} line
     * @param state     the state {@code k}
     * @param relations the relation lines to reason with, or null to find them
     */
    public ModalUntilWitnessLeft(int line, String state, List<Integer> relations) {
        this.line = line;
        this.state = state;
        this.relations = relations == null ? null : List.copyOf(relations);
    }

    private Optional<SortedSet<Integer>> support(ModalNaturalDeduction pf) {
        var witness = ModalUntilWitness.witness(pf, line);
        var term = StateTerm.tryParse(state);
        if (witness.isEmpty() || term.isEmpty() || !term.get().hasSuccessor()) {
            return Optional.empty();
        }
        // The UW line is a relation too (s <= t), and usually the one that puts k before t
        var order = Relations.order(pf, relations == null ? null : withLine(relations));
        if (order.isEmpty()) {
            return Optional.empty();
        }
        var fromS = order.get().entails(witness.get().from(), state);
        var beforeT = order.get().entails(term.get().successor().toString(), witness.get().state());
        if (fromS.isEmpty() || beforeT.isEmpty()) {
            return Optional.empty();
        }
        if (relations != null) {
            return Optional.of(new TreeSet<>(relations));
        }
        SortedSet<Integer> used = new TreeSet<>(fromS.get());
        used.addAll(beforeT.get());
        // Cited first anyway
        used.remove(line);
        return Optional.of(used);
    }

    private List<Integer> withLine(List<Integer> cited) {
        var all = new ArrayList<>(cited);
        if (!all.contains(line)) {
            all.add(line);
        }
        return all;
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
        var left = ModalUntilWitness.witness(pf, line).orElseThrow().until().getLeft();
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), left,
                new ProofReason(ParseModalNextUntilAction.UNTIL_WITNESS_LEFT, List.of(), cited)));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalUntilWitnessLeft other && other.line == line && Objects.equals(other.state, state)
                && Objects.equals(other.relations, relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), line, state, relations);
    }
}
