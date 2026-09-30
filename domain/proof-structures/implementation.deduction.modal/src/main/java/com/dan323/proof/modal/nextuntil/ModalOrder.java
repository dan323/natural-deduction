package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
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
 * Order, {@code Ord [i, j, ...]}: derive {@code u <= v} when the relations on lines {@code i, j, ...} give it in linear
 * discrete time, or {@code FALSE} when they contradict each other ({@link StateOrder}). It covers {@code Refl},
 * {@code Trans} and {@code Succ} and what linear time adds to them: {@code s <= t} gives {@code s+1 <= t+1}, and
 * {@code s+1 <= s} is a contradiction. With no lines it derives what holds whatever the relations are, such as
 * {@code s0 <= s0+2}. A client names the relation (or {@code FALSE}) to derive, and the lines are the ones it needs.
 */
public final class ModalOrder implements ModalAction {

    private final List<Integer> lines;
    private final ModalOperation target;

    /** The rule as a client applies it: the lines are found among the valid relations. */
    public ModalOrder(ModalOperation target) {
        this(null, target);
    }

    /**
     * @param lines  the lines to reason with, or null to find them
     * @param target {@code u <= v} or {@code FALSE}
     */
    public ModalOrder(List<Integer> lines, ModalOperation target) {
        this.lines = lines == null ? null : List.copyOf(lines);
        this.target = target;
    }

    /** @return the lines the rule uses, empty when it does not apply */
    Optional<SortedSet<Integer>> support(ModalNaturalDeduction pf) {
        var order = Relations.order(pf, lines);
        if (order.isEmpty()) {
            return Optional.empty();
        }
        Optional<SortedSet<Integer>> used;
        if (target instanceof LessEqual relation) {
            used = order.get().entails(relation.getLeft(), relation.getRight());
        } else if (ConstantModal.FALSE.equals(target)) {
            used = order.get().contradiction();
        } else {
            used = Optional.empty();
        }
        return lines == null ? used : used.map(found -> new TreeSet<>(lines));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return support(pf).isPresent();
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var used = new ArrayList<>(support(pf).orElseThrow());
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), target,
                new ProofReason(ParseModalNextUntilAction.ORDER, List.of(), used)));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> log instanceof RelationOperation relation
                ? new ProofStepModal(assLevel, relation, reason)
                : new ProofStepModal(pf.getState0(), assLevel, (ModalLogicalOperation) log, reason));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModalOrder order && Objects.equals(order.lines, lines) && order.target.equals(target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), lines, target);
    }
}
