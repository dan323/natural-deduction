package com.dan323.classical.internal;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.DisjunctionClassic;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The automatic solver of intuitionistic logic: the goal-directed solver of classical logic without the two classical
 * steps, double negation elimination ({@code -E}) and proof by contradiction (assuming {@code - A} to reach
 * {@code A}). Every rule it applies, including the composed ones ({@code DeMorgan}: {@code - (A | B)} gives
 * {@code - A} and {@code - B}; {@code OrE1}/{@code OrE2}: {@code A | B} and {@code - A} give {@code B}), is built from
 * intuitionistic rules only.
 *
 * <p>Where classical logic would go for a contradiction, a disjunction {@code A | B} is split instead: the solver
 * tries to reach {@code A} (and then {@code A | B} by {@code |I1}); if that attempt gets stuck it removes every step
 * the attempt added and tries {@code B}. If both get stuck, so does the disjunction. Any other goal of that kind (a
 * variable or {@code TRUE}) just gets stuck. A stuck goal makes the attempt it belongs to fail; with no attempt left
 * the solver gives up and leaves the proof with its premises. So it does not find every intuitionistic proof:
 * leaving a proof unfinished only means that it found none.
 *
 * <p>It always stops. An attempt is never nested inside an attempt at the same disjunction, and every disjunction it
 * attempts is a subformula of the premises or of the goal, so attempts nest at most that many deep. The proof and
 * the goals together are also kept below a size proportional to the size of the premises and the goal
 * ({@link #STEPS_PER_SYMBOL}); a round that would go further counts as stuck. The shared engine needs that bound:
 * {@code OrE1}/{@code OrE2} leave an identity implication {@code B -> B} among the steps, and {@code ->E} with it and
 * the newest {@code B} is a new elimination each time (on {@code p | q, - p} with goal {@code r} it would add copies of
 * {@code q} forever).
 *
 * @author daniel
 */
public final class IntuitionisticAutomate extends GoalDirectedAutomate {

    /**
     * How many steps and goals the proof may have for each symbol of its premises and goal.
     */
    static final int STEPS_PER_SYMBOL = 20;

    /**
     * An attempt at a disjunction: the state to go back to when it fails, and which side is being tried.
     */
    private record Attempt(DisjunctionClassic disjunction, State before, boolean right) {
    }

    private final Deque<Attempt> attempts = new ArrayDeque<>();
    private int maxSize;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public IntuitionisticAutomate() {
        // Nothing to set up: automate initializes the state
    }

    @Override
    protected void started() {
        attempts.clear();
        int symbols = size(proof().getGoal());
        for (ClassicalLogicOperation premise : proof().getAssms()) {
            symbols += size(premise);
        }
        maxSize = STEPS_PER_SYMBOL * symbols;
    }

    private static int size(LogicOperation formula) {
        if (formula instanceof UnaryOperation<?> unary) {
            return 1 + size(unary.getElement());
        } else if (formula instanceof BinaryOperation<?> binary) {
            return 1 + size(binary.getLeft()) + size(binary.getRight());
        }
        return 1;
    }

    @Override
    protected boolean eliminatesDoubleNegations() {
        return false;
    }

    @Override
    protected boolean withinBounds() {
        return proof().getSteps().size() + goalCount() <= maxSize;
    }

    /**
     * Attempt the left side of a disjunction, unless an attempt at the same disjunction is already going on. Any
     * other goal gets stuck.
     *
     * @param goal last goal
     */
    @Override
    protected void updateOtherGoal(ClassicalLogicOperation goal) {
        if (goal instanceof DisjunctionClassic disjunction && attempts.stream().noneMatch(attempt -> attempt.disjunction().equals(disjunction))) {
            attempts.push(new Attempt(disjunction, state(), false));
            pushGoal(disjunction.getLeft());
        }
    }

    /**
     * The side of a disjunction that is being attempted was reached, so the attempt succeeded: there is no going back
     * on it anymore. The side is the goal just above the disjunction, which is the last goal when the attempt starts.
     */
    @Override
    protected void goalRemoved() {
        while (!attempts.isEmpty() && goalCount() <= attempts.peek().before().goals().size()) {
            attempts.pop();
        }
    }

    /**
     * The innermost attempt failed: remove its steps and try the right side if it was on the left one. When both
     * sides of a disjunction failed, the attempt that needed the disjunction fails too. With no attempt left the
     * solver gives up and leaves the proof with its premises.
     */
    @Override
    protected boolean stalled() {
        while (!attempts.isEmpty()) {
            Attempt failed = attempts.pop();
            restore(failed.before());
            if (!failed.right()) {
                attempts.push(new Attempt(failed.disjunction(), failed.before(), true));
                pushGoal(failed.disjunction().getRight());
                return true;
            }
        }
        proof().reset();
        return false;
    }
}
