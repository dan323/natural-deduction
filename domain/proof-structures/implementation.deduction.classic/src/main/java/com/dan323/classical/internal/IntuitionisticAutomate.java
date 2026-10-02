package com.dan323.classical.internal;

import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicModusPonens;
import com.dan323.classical.ClassicOrE;
import com.dan323.classical.ClassicalAction;
import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ConstantClassic;
import com.dan323.expressions.classical.DisjunctionClassic;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.NegationClassic;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The automatic solver of intuitionistic logic: the goal-directed solver of classical logic without the two classical
 * steps, double negation elimination ({@code -E}) and proof by contradiction (assuming {@code - A} to reach
 * {@code A}). Every rule it applies, including the composed ones ({@code DeMorgan}: {@code - (A | B)} gives
 * {@code - A} and {@code - B}; {@code OrE1}/{@code OrE2}: {@code A | B} and {@code - A} give {@code B}), is built from
 * intuitionistic rules only.
 *
 * <p>Where classical logic would go for a contradiction, a goal {@code G} that no introduction rule reaches (a
 * disjunction, a variable or {@code TRUE}) gets attempts instead, tried in this order:
 * <ol>
 *     <li>a case split: for each valid step {@code A | B}, reach {@code A -> G} (assume {@code A}, reach {@code G},
 *     {@code ->I}) and {@code B -> G}, then {@code |E}. It comes first because it loses nothing: {@code G} follows
 *     from the steps exactly when it follows in both cases, and each case has one more step to use;</li>
 *     <li>if {@code G} is a disjunction {@code A | B}, reach {@code A} (then {@code |I1}), else {@code B} (then
 *     {@code |I2});</li>
 *     <li>backwards through an implication: for each valid step {@code A -> G}, reach {@code A}, then {@code ->E};</li>
 *     <li>ex falso: reach {@code FALSE}, then {@code FE}. It is the last one because it gives up on the shape of
 *     {@code G} altogether and it only helps when the steps are contradictory; it is not tried when no valid step
 *     has a negation or {@code FALSE} in it, since such steps are all true when every variable is.</li>
 * </ol>
 * The last rule of each attempt is found by {@link #reachGoal} once the subgoals are reached. If an attempt gets stuck
 * the solver removes every step it added and tries the next one; if every attempt at {@code G} gets stuck, so does
 * {@code G}. A stuck goal makes the attempt it belongs to fail; with no attempt left the solver gives up and leaves the
 * proof with its premises. An attempt that reached its subgoals is kept: the solver never goes back to try another
 * one for the same goal. So it does not find every intuitionistic proof: leaving a proof unfinished only means that it
 * found none.
 *
 * <p>It always stops. An attempt is never nested inside another attempt of the same kind on the same formula (the
 * disjunction goal, the implication or the disjunction step, and ex falso at most once), so attempts nest at most as
 * deep as there are such formulas. The proof and the goals together are also kept below a size proportional to the
 * size of the premises and the goal ({@link #STEPS_PER_SYMBOL}); a round that would go further counts as stuck. The
 * bound is a second line of defence: the engine no longer applies {@code ->E} to the identity implication
 * {@code B -> B} that {@code OrE1}/{@code OrE2} leave among the steps (#192).
 *
 * @author daniel
 */
public final class IntuitionisticAutomate extends GoalDirectedAutomate {

    /**
     * How many steps and goals the proof may have for each symbol of its premises and goal.
     */
    static final int STEPS_PER_SYMBOL = 20;

    private enum Strategy {
        SIDES, BACKWARDS, CASES, EX_FALSO
    }

    /**
     * What an attempt is about: an attempt is never nested inside another one with the same key.
     *
     * @param strategy the kind of attempt
     * @param formula  the disjunction goal, the implication or disjunction step, or {@code FALSE} for ex falso
     */
    private record Key(Strategy strategy, ClassicalLogicOperation formula) {
    }

    /**
     * One way to reach a goal: the subgoals it pushes.
     */
    private record Alternative(Key key, List<ClassicalLogicOperation> subgoals) {
    }

    /**
     * An attempt at a goal: the state to go back to when it fails, what it is about and the ways still to try.
     */
    private record Attempt(State before, Key key, List<Alternative> remaining) {
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
     * Start the first attempt at a goal that no introduction rule reaches. With no attempt to make, the goal gets
     * stuck.
     *
     * @param goal last goal
     */
    @Override
    protected void updateOtherGoal(ClassicalLogicOperation goal) {
        List<Alternative> alternatives = alternatives(goal);
        if (!alternatives.isEmpty()) {
            start(state(), alternatives);
        }
    }

    private void start(State before, List<Alternative> alternatives) {
        Alternative first = alternatives.get(0);
        attempts.push(new Attempt(before, first.key(), alternatives.subList(1, alternatives.size())));
        first.subgoals().forEach(this::pushGoal);
    }

    /**
     * The attempts at a goal, in the order they are tried. The subgoals of each one are pushed in order, so the last
     * one is reached first.
     */
    private List<Alternative> alternatives(ClassicalLogicOperation goal) {
        List<Alternative> alternatives = new ArrayList<>();
        List<ClassicalLogicOperation> steps = validSteps();
        for (ClassicalLogicOperation step : steps) {
            if (step instanceof DisjunctionClassic disjunction) {
                addIfFree(alternatives, new Key(Strategy.CASES, disjunction), List.of(
                        new ImplicationClassic(disjunction.getRight(), goal),
                        new ImplicationClassic(disjunction.getLeft(), goal)));
            }
        }
        if (goal instanceof DisjunctionClassic disjunction) {
            Key key = new Key(Strategy.SIDES, disjunction);
            if (isFree(key)) {
                alternatives.add(new Alternative(key, List.of(disjunction.getLeft())));
                alternatives.add(new Alternative(key, List.of(disjunction.getRight())));
            }
        }
        for (ClassicalLogicOperation step : steps) {
            // An implication A -> A is left by OrE1/OrE2: its A would only be the goal again
            if (step instanceof ImplicationClassic implication && implication.getRight().equals(goal)
                    && !implication.getLeft().equals(goal)) {
                addIfFree(alternatives, new Key(Strategy.BACKWARDS, implication), List.of(implication.getLeft()));
            }
        }
        // Steps without a negation or FALSE are all true when every variable is: they cannot give FALSE
        if (steps.stream().anyMatch(IntuitionisticAutomate::mentionsFalse)) {
            addIfFree(alternatives, new Key(Strategy.EX_FALSO, ConstantClassic.FALSE), List.of(ConstantClassic.FALSE));
        }
        return alternatives;
    }

    private static boolean mentionsFalse(LogicOperation formula) {
        if (formula instanceof NegationClassic || formula.equals(ConstantClassic.FALSE)) {
            return true;
        } else if (formula instanceof BinaryOperation<?> binary) {
            return mentionsFalse(binary.getLeft()) || mentionsFalse(binary.getRight());
        }
        return false;
    }

    private void addIfFree(List<Alternative> alternatives, Key key, List<ClassicalLogicOperation> subgoals) {
        if (isFree(key) && alternatives.stream().noneMatch(alternative -> alternative.key().equals(key))) {
            alternatives.add(new Alternative(key, subgoals));
        }
    }

    /**
     * Whether no attempt going on has this key.
     */
    private boolean isFree(Key key) {
        return attempts.stream().noneMatch(attempt -> attempt.key().equals(key));
    }

    private List<ClassicalLogicOperation> validSteps() {
        return proof().getSteps().stream().filter(ProofStep::isValid).map(ProofStep::getStep).toList();
    }

    /**
     * The last rule of each attempt: {@code FE} from {@code FALSE}, {@code ->E} from {@code A -> G} and {@code A},
     * or {@code |E} from {@code A | B}, {@code A -> G} and {@code B -> G}.
     */
    @Override
    protected ClassicalAction reachGoal(ClassicalLogicOperation goal) {
        if (goal.equals(ConstantClassic.FALSE)) {
            return null;
        }
        List<ProofStep<ClassicalLogicOperation>> steps = proof().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).isValid()) {
                ClassicalAction action = reachGoalFrom(i, goal);
                if (action != null && action.isValid(proof())) {
                    return action;
                }
            }
        }
        return null;
    }

    private ClassicalAction reachGoalFrom(int i, ClassicalLogicOperation goal) {
        ClassicalLogicOperation step = proof().getSteps().get(i).getStep();
        if (step.equals(ConstantClassic.FALSE)) {
            return new ClassicFE(i + 1, goal);
        } else if (step instanceof ImplicationClassic implication && implication.getRight().equals(goal)) {
            int antecedent = validStep(implication.getLeft());
            return antecedent > 0 ? new ClassicModusPonens(i + 1, antecedent) : null;
        } else if (step instanceof DisjunctionClassic disjunction) {
            int left = validStep(new ImplicationClassic(disjunction.getLeft(), goal));
            int right = validStep(new ImplicationClassic(disjunction.getRight(), goal));
            return left > 0 && right > 0 ? new ClassicOrE(i + 1, left, right) : null;
        }
        return null;
    }

    /**
     * The 1-based line of a valid step with this formula, or 0 if there is none.
     */
    private int validStep(ClassicalLogicOperation formula) {
        List<ProofStep<ClassicalLogicOperation>> steps = proof().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).isValid() && steps.get(i).getStep().equals(formula)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * The subgoals of an attempt were reached, so it succeeded: there is no going back on it anymore. Its subgoals
     * are the goals above the one it is about, which is the last goal when the attempt starts.
     */
    @Override
    protected void goalRemoved() {
        while (!attempts.isEmpty() && goalCount() <= attempts.peek().before().goals().size()) {
            attempts.pop();
        }
    }

    /**
     * The innermost attempt failed: remove its steps and try the next way to reach its goal. When every way failed,
     * the attempt that needed the goal fails too. With no attempt left the solver gives up and leaves the proof with
     * its premises.
     */
    @Override
    protected boolean stalled() {
        while (!attempts.isEmpty()) {
            Attempt failed = attempts.pop();
            restore(failed.before());
            if (!failed.remaining().isEmpty()) {
                start(failed.before(), failed.remaining());
                return true;
            }
        }
        proof().reset();
        return false;
    }
}
