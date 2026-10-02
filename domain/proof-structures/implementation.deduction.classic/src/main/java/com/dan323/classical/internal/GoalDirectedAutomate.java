package com.dan323.classical.internal;

import com.dan323.classical.*;
import com.dan323.classical.complex.DeMorgan;
import com.dan323.classical.complex.OrE1;
import com.dan323.classical.complex.OrE2;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.classical.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * The goal-directed solver that the classical and the intuitionistic solvers share.
 *
 * <p>It keeps a stack of goals, the proof's goal at the bottom. Each round it reaches the top goal with an
 * introduction rule if it can, otherwise it applies the elimination rules it has not applied yet; when neither is
 * possible it pushes subgoals (and assumptions) that make the top goal easier to reach. It stops when the stack is
 * empty (the proof is done) or when a round changes neither the proof nor the goals.
 *
 * <p>The subclasses decide what differs between the logics: whether double negation elimination ({@code -E}) is one
 * of the elimination rules, what to do with a goal that no introduction rule and no subgoal of its shape can reach
 * ({@link #updateOtherGoal}), which other rules reach a goal ({@link #reachGoal}), what to do when a round changes
 * nothing ({@link #stalled()}) and how far the search may go ({@link #withinBounds()}).
 *
 * @author daniel
 */
abstract class GoalDirectedAutomate {

    private NaturalDeduction proof;
    private List<ClassicalLogicOperation> goals;
    private List<ClassicalAction> actionsDone;
    private Map<Integer, Integer> usedForGoal;

    /**
     * The working state of the solver at some point, to go back to it with {@link #restore(State)}.
     *
     * @param steps       the number of steps of the proof
     * @param goals       the goals
     * @param actionsDone the elimination rules already applied
     * @param usedForGoal the steps already used to find a subgoal of {@link ConstantClassic#FALSE}
     */
    protected record State(int steps, List<ClassicalLogicOperation> goals, List<ClassicalAction> actionsDone,
                           Map<Integer, Integer> usedForGoal) {
    }

    /**
     * Finish the proof if it can be done.
     * It will stop without solving it if it cannot be solved
     *
     * <p>It checks the interrupt flag of the calling thread between its steps: interrupting the thread stops it
     * with a {@link CancellationException}.
     *
     * @param naturalDeduction the proof to solve
     * @throws CancellationException if the calling thread is interrupted
     */
    public void automate(NaturalDeduction naturalDeduction) {
        // Init state
        proof = naturalDeduction;
        proof.reset();
        goals = new ArrayList<>();
        actionsDone = new ArrayList<>();
        usedForGoal = new HashMap<>();
        goals.add(proof.getGoal());
        started();

        boolean c = true;
        while (c) {
            checkNotInterrupted();
            int goalSize = goals.size();
            int stepsSize = proof.getSteps().size();
            c = applyIntroAndElimRules();
            if (c) {
                updateGoal();
                // If the state of the proof has not changed, stop unless the subclass finds a way out. It has failed
                c = (isStateChanged(goalSize, stepsSize) && withinBounds()) || stalled();
            }
        }
    }

    /**
     * Called once the state is initialized, before the first round.
     */
    protected void started() {
        // Nothing to set up by default
    }

    /**
     * Whether double negation elimination ({@link ClassicNotE}) is one of the elimination rules.
     */
    protected abstract boolean eliminatesDoubleNegations();

    /**
     * Push subgoals for a goal that no introduction rule reaches and that is not {@link ConstantClassic#FALSE}, a
     * conjunction, an implication or a negation: a disjunction, a variable or {@link ConstantClassic#TRUE}.
     * Changing nothing makes the round a stalled one.
     *
     * @param goal the last goal
     */
    protected abstract void updateOtherGoal(ClassicalLogicOperation goal);

    /**
     * Called when a round changed neither the proof nor the goals.
     *
     * @return true to go on with another round (the state must have changed), false to stop
     */
    protected abstract boolean stalled();

    /**
     * Whether the solver may go on: once it returns false, the solver applies no more elimination rules and treats
     * the round as a stalled one.
     */
    protected boolean withinBounds() {
        return true;
    }

    /**
     * A rule, other than the introduction rules of the engine, that reaches the goal from the valid steps.
     *
     * @param goal the last goal
     * @return a valid rule whose conclusion is the goal, or null (the default) when there is none
     */
    protected ClassicalAction reachGoal(ClassicalLogicOperation goal) {
        return null;
    }

    /**
     * Called after the last goal was removed from the stack because it was reached.
     */
    protected void goalRemoved() {
        // Nothing to do by default
    }

    protected final NaturalDeduction proof() {
        return proof;
    }

    protected final int goalCount() {
        return goals.size();
    }

    protected final void pushGoal(ClassicalLogicOperation goal) {
        goals.add(goal);
    }

    /**
     * The current state, which later changes of the solver do not modify.
     */
    protected final State state() {
        return new State(proof.getSteps().size(), new ArrayList<>(goals), new ArrayList<>(actionsDone), new HashMap<>(usedForGoal));
    }

    /**
     * Goes back to an earlier state: the proof loses the steps added since then (it is rebuilt from its premises by
     * replaying the steps it keeps, so a step that a later rule discharged is valid again) and the goals and the
     * applied rules are the ones of then.
     *
     * @param state a state of the current proof, taken with {@link #state()}
     */
    protected final void restore(State state) {
        List<ClassicalAction> actions = proof.parse();
        proof.reset();
        for (int i = proof.getAssms().size(); i < state.steps(); i++) {
            actions.get(i).apply(proof);
        }
        goals = new ArrayList<>(state.goals());
        actionsDone = new ArrayList<>(state.actionsDone());
        usedForGoal = new HashMap<>(state.usedForGoal());
    }

    private static void checkNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("The automatic solver was interrupted");
        }
    }

    /**
     * Checks if the state of the proof has changed according to previous sizes
     *
     * @param previousGoalSize number of goals before the actions
     * @param previousStepSize size of the proof before the actions
     * @return true iff the number of goals of the proof has more steps
     */
    private boolean isStateChanged(int previousGoalSize, int previousStepSize) {
        return previousGoalSize != goals.size() || previousStepSize != proof.getSteps().size();
    }

    /**
     * Look for intro rules for the last goal, if none found look and apply eliminate rules
     *
     * @return false iff the proof is finished
     */
    private boolean applyIntroAndElimRules() {
        boolean b = true;
        boolean c = true;
        while (b) {
            checkNotInterrupted();
            ClassicalAction intro = introRuleForGoal();
            if (intro != null || isGoalReached()) {
                updateGoals(intro);
                if (goals.isEmpty()) {
                    c = false;
                    b = false;
                }
            } else {
                b = withinBounds() && eliminateRules();
            }
        }
        return c;
    }

    /**
     * Check if the goal was reabhed
     *
     * @return true if the last goal is equal to the last statement in the proof
     */
    private boolean isGoalReached() {
        return !proof.getSteps().isEmpty() &&
                goals.get(goals.size() - 1).equals(proof.getSteps().get(proof.getSteps().size() - 1).getStep());
    }

    /**
     * Look for an elimination rule, and apply it.
     *
     * @return true iff a rule was found and was applied
     */
    private boolean eliminateRules() {
        ClassicalAction ca = lookForElimRules();
        if (ca != null) {
            ca.apply(proof);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Apply the rule that attains the next goal
     *
     * @param intro action that will attain the goal
     */
    private void updateGoals(ClassicalAction intro) {
        if (intro != null) {
            intro.apply(proof);
        }
        attainFalseGoal();
        goals.remove(goals.size() - 1);
        usedForGoal.remove(goals.size() + 2);
        goalRemoved();
    }

    /**
     * If the last goal is {@literal False}, the goal below it is a negation and the rule NotI is valid, we use it to
     * reach that negation. A {@literal False} goal below which there is no negation (the right side of an implication
     * goal {@code A -> FALSE}) is only removed: {@code -I} would give {@code - A}, not the goal, so the next round
     * reaches the implication with {@code ->I} instead.
     */
    private void attainFalseGoal() {
        if (goals.size() > 1 && goals.get(goals.size() - 1).equals(ConstantClassic.FALSE)
                && goals.get(goals.size() - 2) instanceof NegationClassic) {
            ClassicalAction cla = new ClassicNotI();
            if (cla.isValid(proof)) {
                cla.apply(proof);
                goals.remove(goals.size() - 1);
                goalRemoved();
            }
        }
    }

    /**
     * Check if last goal can be reached with an intro rule
     *
     * @return the action that must be applied to reach the goal
     */
    private ClassicalAction introRuleForGoal() {
        if (proof.getSteps().isEmpty()) {
            return null;
        }
        ClassicalLogicOperation goal = goals.get(goals.size() - 1);
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() &&
                    goal.equals(proof.getSteps().get(i).getStep()) &&
                    i + 1 < proof.getSteps().size()) {
                return new ClassicCopy(i + 1);
            }
        }
        ClassicalAction sol = null;
        if (goal instanceof ConjunctionClassic) {
            sol = introRuleForGoalConjuntion((ConjunctionClassic) goal);
        } else if (goal instanceof ImplicationClassic) {
            sol = introRuleForGoalImplication((ImplicationClassic) goal);
        } else if (goal instanceof DisjunctionClassic) {
            sol = introRuleForGoalDisjunction((DisjunctionClassic) goal);
        } else if (goal instanceof NegationClassic) {
            sol = introRuleForGoalNegation((NegationClassic) goal);
        } else if (goal.equals(ConstantClassic.FALSE)) {
            sol = introRuleForGoalContradiction();
        }
        if (sol == null) {
            sol = reachGoal(goal);
        }
        return sol;
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ConstantClassic#FALSE}
     *
     * @return the action {@link ClassicFI} that must be used or null
     */
    private ClassicalAction introRuleForGoalContradiction() {
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep() instanceof NegationClassic) {
                ClassicalLogicOperation element = ((NegationClassic) proof.getSteps().get(i).getStep()).getElement();
                for (int j = 0; j < proof.getSteps().size(); j++) {
                    if (proof.getSteps().get(j).isValid() && proof.getSteps().get(j).getStep().equals(element)) {
                        return new ClassicFI(j + 1, i + 1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link NegationClassic}
     *
     * @return the action {@link ClassicNotI} that must be used or null
     */
    private ClassicalAction introRuleForGoalNegation(NegationClassic goal) {
        ClassicalLogicOperation element = goal.getElement();
        int i = 0;
        int assmsLevel = proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep().equals(element)) {
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(ConstantClassic.FALSE)) {
                    if (k + 1 < proof.getSteps().size()) {
                        (new ClassicCopy(k + 1)).apply(proof);
                    }
                    return new ClassicNotI();
                }
            }
        }
        return null;
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link DisjunctionClassic}
     *
     * @return the action {@link ClassicOrI1} or {@link ClassicOrI2} that must be used or null
     */
    private ClassicalAction introRuleForGoalDisjunction(DisjunctionClassic goal) {
        ClassicalLogicOperation left = (goal).getLeft();
        ClassicalLogicOperation right = (goal).getRight();
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid()) {
                if (proof.getSteps().get(k).getStep().equals(left)) {
                    return new ClassicOrI1(k + 1, right);
                }
                if (proof.getSteps().get(k).getStep().equals(right)) {
                    return new ClassicOrI2(k + 1, left);
                }
            }
        }
        return null;
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ConjunctionClassic}
     *
     * @return the action {@link ClassicAndI} that must be used or null
     */
    private ClassicalAction introRuleForGoalConjuntion(ConjunctionClassic goal) {
        ClassicalLogicOperation left = (goal).getLeft();
        int a = 0;
        ClassicalLogicOperation right = (goal).getRight();
        int b = 0;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid()) {
                if (left.equals(proof.getSteps().get(i).getStep())) {
                    a = i + 1;
                }
                if (right.equals(proof.getSteps().get(i).getStep())) {
                    b = i + 1;
                }
            }
            if (a > 0 && b > 0) {
                return new ClassicAndI(a, b);
            }
        }
        return null;
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ImplicationClassic}
     *
     * @return the action {@link ClassicDeductionTheorem} that must be used or null
     */
    private ClassicalAction introRuleForGoalImplication(ImplicationClassic goal) {
        ClassicalLogicOperation left = (goal).getLeft();
        ClassicalLogicOperation right = (goal).getRight();
        int i = 0;
        int assmsLevel = proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep().equals(left)) {
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(right)) {
                    if (k + 1 < proof.getSteps().size() && !proof.getSteps().get(proof.getSteps().size() - 1).getStep().equals(proof.getSteps().get(k).getStep())) {
                        (new ClassicCopy(k + 1)).apply(proof);
                    }
                    return new ClassicDeductionTheorem();
                }
            }
        }
        return null;
    }

    private ClassicalAction lookForElimRules() {
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid()) {
                ClassicalAction act = new DeMorgan(i + 1);
                act = checkSingleAction(act);
                if (act != null) {
                    return act;
                }
                if (eliminatesDoubleNegations()) {
                    act = new ClassicNotE(i + 1);
                    act = checkSingleAction(act);
                    if (act != null) {
                        return act;
                    }
                }
                act = checkAdditionOfAndI(i);
                if (act != null) {
                    return act;
                }
                act = checkAdditionOfDisjIModPonens(i);
                if (act != null) {
                    return act;
                }
            }
        }
        return null;
    }

    private ClassicalAction checkAdditionOfAndI(int i) {
        ClassicalAction act = new ClassicAndE1(i + 1);
        act = checkSingleAction(act);
        if (act == null) {
            act = new ClassicAndE2(i + 1);
            act = checkSingleAction(act);
        }
        return act;
    }

    /**
     * Check that the action has not already done ant it is valid
     *
     * @param act action to be checked for validity
     * @return the action if it valid, null if not
     */
    private ClassicalAction checkSingleAction(ClassicalAction act) {
        if (!actionsDone.contains(act) && act.isValid(proof)) {
            actionsDone.add(act);
            return act;
        }
        return null;
    }

    /**
     * {@code B -> B}, which {@link OrE1} and {@link OrE2} leave in the proof: {@code ->E} on it only repeats
     * {@code B}, once more for every new line {@code B}, so the elimination loop would never end.
     */
    private static boolean isIdentityImplication(ClassicalLogicOperation step) {
        return step instanceof ImplicationClassic imp && imp.getLeft().equals(imp.getRight());
    }

    private ClassicalAction checkAdditionOfDisjIModPonens(int i) {
        for (int j = 0; j < proof.getSteps().size(); j++) {
            if (proof.getSteps().get(j).isValid()) {
                ClassicalAction act = null;
                if (!isIdentityImplication(proof.getSteps().get(i).getStep())) {
                    act = checkSingleAction(new ClassicModusPonens(i + 1, j + 1));
                }
                if (act != null) {
                    return act;
                }
                act = new OrE1(i + 1, j + 1);
                act = checkSingleAction(act);
                if (act != null) {
                    return act;
                }
                act = new OrE2(i + 1, j + 1);
                act = checkSingleAction(act);
                if (act != null) {
                    return act;
                }
            }
        }
        return null;
    }

    /**
     * Add a new goal to make easier to reach the current goal, if possible
     */
    private void updateGoal() {
        if (goals.get(goals.size() - 1).equals(ConstantClassic.FALSE)) {
            lastGoalFalse();
        } else {
            ClassicalLogicOperation goal = goals.get(goals.size() - 1);
            if (goal instanceof ConjunctionClassic) {
                updateGoalConjuntion((ConjunctionClassic) goal);
            } else if (goal instanceof ImplicationClassic) {
                updateGoalImplication((ImplicationClassic) goal);
            } else if (goal instanceof NegationClassic) {
                updateGoalNegation((NegationClassic) goal);
            } else {
                updateOtherGoal(goal);
            }
        }
    }

    /**
     * Update the goal list in case the last goal is {@link ConstantClassic#FALSE}
     */
    private void lastGoalFalse() {
        int j = -1;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            boolean b = true;
            if (!usedForGoal.containsValue(i) && proof.getSteps().get(i).isValid()) {
                LogicOperation log = proof.getSteps().get(i).getStep();
                if (log instanceof NegationClassic) {
                    goals.add(((NegationClassic) log).getElement());
                } else if (log instanceof DisjunctionClassic) {
                    goals.add(new NegationClassic(((DisjunctionClassic) log).getLeft()));
                } else if (log instanceof ImplicationClassic) {
                    goals.add(((ImplicationClassic) log).getLeft());
                } else {
                    b = false;
                }
                if (b) {
                    j = i;
                    break;
                }
            }
        }
        usedForGoal.put(goals.size(), j);
    }

    /**
     * Create new goal a assumption from the last goal of type -
     *
     * @param goal last goal of type -
     */
    private void updateGoalNegation(NegationClassic goal) {
        goals.add(ConstantClassic.FALSE);
        (new ClassicAssume((goal).getElement())).apply(proof);
    }

    /**
     * Create new goal a assumption from the last goal of type ->
     *
     * @param goal last goal of type ->
     */
    private void updateGoalImplication(ImplicationClassic goal) {
        goals.add((goal).getRight());
        (new ClassicAssume((goal).getLeft())).apply(proof);
    }

    /**
     * Create new goals from the last goal of type AND
     *
     * @param goal last goal of type AND
     */
    private void updateGoalConjuntion(ConjunctionClassic goal) {
        goals.add(goal.getLeft());
        goals.add(goal.getRight());
    }
}
