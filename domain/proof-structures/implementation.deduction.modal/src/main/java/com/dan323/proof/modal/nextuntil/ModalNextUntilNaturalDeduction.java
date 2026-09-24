package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.relation.RelationOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A proof of {@code modal-next-until}: modal logic over discrete states, where every state {@code s} has a successor
 * {@code s+1} ({@link StateTerm}), with Next ({@code X A}) and Until ({@code A U B}).
 * <p>
 * The modal rules keep their meaning. What changes is freshness: {@code s0+1} is not a new state but the successor of
 * {@code s0}, so a state is used when its base is ({@code s0} is used once {@code s0+2} is), and the fresh state that
 * {@code []I} and {@code <>E} introduce must be a base that is used nowhere before, not even on the other side of its
 * relation ({@code t+1 <= t} would say that {@code t} comes after itself).
 * <p>
 * This logic has no solver: the modal one uses none of the Next and Until rules.
 */
public class ModalNextUntilNaturalDeduction extends ModalNaturalDeduction {

    public ModalNextUntilNaturalDeduction(String state0) {
        super(state0);
    }

    public ModalNextUntilNaturalDeduction() {
        super();
    }

    @Override
    public List<AbstractModalAction> parse() {
        return ((ParseAction<AbstractModalAction, ModalNaturalDeduction, com.dan323.expressions.modal.ModalOperation, ProofStepModal>) ParseModalNextUntilAction::parse)
                .translateToActions(this);
    }

    /**
     * @return whether the base of {@code state} is the base of the initial state or of a state that a valid step before
     * {@code k} is in or relates
     */
    @Override
    public boolean stateIsUsedBefore(String state, int k) {
        var base = base(state);
        boolean used = base.equals(base(getState0()));
        for (int i = 0; i < k && !used; i++) {
            var step = getSteps().get(i);
            if (step.isValid()) {
                used = basesOf(step).contains(base);
            }
        }
        return used;
    }

    /**
     * @return whether {@code state} is a base ({@code t}, not {@code t+1}) that no valid step before {@code k} uses, and
     * that {@code from} does not use either
     */
    @Override
    public boolean isFreshState(String state, String from, int k) {
        StateTerm term;
        try {
            term = StateTerm.parse(state);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return term.isBase() && !term.base().equals(base(from)) && !stateIsUsedBefore(state, k);
    }

    /** @return a base state that no valid step uses */
    @Override
    public String newState() {
        Set<String> bases = new HashSet<>();
        bases.add(base(getState0()));
        for (var step : getSteps()) {
            if (step.isValid()) {
                bases.addAll(basesOf(step));
            }
        }
        int i = bases.size();
        while (bases.contains("s" + i)) {
            i++;
        }
        return "s" + i;
    }

    /**
     * @return whether a valid top-level step is the goal in the initial state (a relation goal has no state). Here the
     * state matters: {@code X p} proves {@code p} in {@code s0+1}, not in {@code s0}.
     */
    @Override
    public boolean isDone() {
        var goal = getGoal();
        if (goal == null) {
            return false;
        }
        return getSteps().stream()
                .filter(step -> step.isValid() && step.getAssumptionLevel() == 0 && goal.equals(step.getStep()))
                .anyMatch(step -> step.getStep() instanceof RelationOperation || getState0().equals(step.getState()));
    }

    /**
     * @throws UnsupportedOperationException always: this logic has no solver
     */
    @Override
    public void automate() {
        throw new UnsupportedOperationException("modal-next-until has no solver");
    }

    private static Set<String> basesOf(ProofStepModal step) {
        if (step.getStep() instanceof RelationOperation relation) {
            // Both sides may have the same base (s0 <= s0+1), which Set.of rejects.
            return new HashSet<>(List.of(base(relation.getLeft()), base(relation.getRight())));
        }
        return Set.of(base(step.getState()));
    }

    /** The base of a state term, or the text itself when it is not one (so that it is only equal to itself). */
    static String base(String state) {
        if (state == null) {
            return "";
        }
        try {
            return StateTerm.parse(state).base();
        } catch (IllegalArgumentException e) {
            return state;
        }
    }
}
