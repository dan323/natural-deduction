package com.dan323.proof.modal.internal;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.complex.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;

import java.util.*;
import java.util.concurrent.CancellationException;

/**
 * Class to execute a Natural deduction in modal logic
 *
 * <p>It keeps a stack of goals, each one a formula in a state, with the proof's goal in the initial state at the
 * bottom. Each round it reaches the top goal with an introduction rule if it can, otherwise it applies the elimination
 * rules it has not applied yet; when neither is possible it pushes subgoals (and assumptions) that make the top goal
 * easier to reach. It stops when the stack is empty (the proof is done) or when a round changes neither the proof nor
 * the goals.
 *
 * <p>A subclass can add rules for the operators of its own logic through the protected hooks: another introduction
 * rule for a goal ({@link #introRuleForOtherGoal}), another elimination rule ({@link #elimRuleForOtherStep}), subgoals
 * for a goal of another shape ({@link #updateOtherGoal}), what to do when a round changes nothing ({@link #stalled()})
 * and how far the search may go ({@link #withinBounds()}). Their defaults are the behaviour of the modal solver.
 *
 * @author daniel
 */
public class ModalAutomate {

    private ModalNaturalDeduction proof;
    private List<Map.Entry<String, ModalOperation>> goals;
    private List<AbstractModalAction> actionsDone;
    private Map<Integer, Integer> usedForGoal;

    private Map<String, Integer> reflUsed;

    /**
     * The working state of the solver at some point, to go back to it with {@link #restore(State)}.
     *
     * @param steps       the number of steps of the proof
     * @param goals       the goals, each one a state and a formula
     * @param actionsDone the elimination rules already applied
     * @param usedForGoal the steps already used to find a subgoal of {@link ConstantModal#FALSE}
     * @param reflUsed    the states that {@code Refl} was already applied to
     */
    protected record State(int steps, List<Map.Entry<String, ModalOperation>> goals,
                           List<AbstractModalAction> actionsDone, Map<Integer, Integer> usedForGoal,
                           Map<String, Integer> reflUsed) {
    }

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public ModalAutomate() {
        // Nothing to set up: automate initializes the state
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
    public void automate(ModalNaturalDeduction naturalDeduction) {
        // Init state
        proof = naturalDeduction;
        proof.reset();
        goals = new ArrayList<>();
        actionsDone = new ArrayList<>();
        usedForGoal = new HashMap<>();
        reflUsed = new HashMap<>();
        goals.add(new AbstractMap.SimpleEntry<>(proof.getState0(), proof.getGoal()));
        started();

        var c = true;
        while (c) {
            checkNotInterrupted();
            var goalSize = goals.size();
            var stepsSize = proof.getSteps().size();
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
     * Called when a round changed neither the proof nor the goals, or went beyond {@link #withinBounds()}.
     *
     * @return true to go on with another round (the state must have changed), false (the default) to stop
     */
    protected boolean stalled() {
        return false;
    }

    /**
     * Whether the solver may go on: once it returns false, the solver applies no more elimination rules and treats
     * the round as a stalled one. The default has no bound.
     */
    protected boolean withinBounds() {
        return true;
    }

    /**
     * An introduction rule for a goal that none of the modal introduction rules reaches.
     *
     * @param goal  the last goal
     * @param state the state of the last goal
     * @return a valid rule whose conclusion is the goal in its state, or empty (the default) when there is none
     */
    protected Optional<AbstractModalAction> introRuleForOtherGoal(ModalOperation goal, String state) {
        return Optional.empty();
    }

    /**
     * An elimination rule, other than the modal ones, on a valid formula step. Use {@link #checkSingleAction} so that
     * each rule is applied once.
     *
     * @param i the 0-based index of the step
     * @return a valid rule not applied yet, or empty (the default) when there is none
     */
    protected Optional<AbstractModalAction> elimRuleForOtherStep(int i) {
        return Optional.empty();
    }

    /**
     * Whether a valid elimination rule not applied yet is worth applying. The default applies all of them.
     *
     * @param act a valid elimination rule
     * @return true (the default) to apply it
     */
    protected boolean isUsefulElimination(AbstractModalAction act) {
        return true;
    }

    /**
     * Push subgoals for a goal that no introduction rule reaches and that is not {@link ConstantModal#FALSE}, a
     * conjunction, an implication, a negation or a {@code []}.
     *
     * @param goal  the last goal
     * @param state the state of the last goal
     * @return true if the goal was handled (changing nothing makes the round a stalled one), false (the default) to
     * go for a contradiction, as the modal solver does
     */
    protected boolean updateOtherGoal(ModalLogicalOperation goal, String state) {
        return false;
    }

    /**
     * Called after the last goal was removed from the stack because it was reached.
     */
    protected void goalRemoved() {
        // Nothing to do by default
    }

    protected final ModalNaturalDeduction proof() {
        return proof;
    }

    protected final int goalCount() {
        return goals.size();
    }

    /**
     * @return the goals (each one a state and a formula), from the bottom of the stack to the top
     */
    protected final List<Map.Entry<String, ModalOperation>> goals() {
        return Collections.unmodifiableList(goals);
    }

    protected final void pushGoal(String state, ModalOperation goal) {
        goals.add(new AbstractMap.SimpleEntry<>(state, goal));
    }

    /**
     * The current state, which later changes of the solver do not modify.
     */
    protected final State state() {
        return new State(proof.getSteps().size(), new ArrayList<>(goals), new ArrayList<>(actionsDone),
                new HashMap<>(usedForGoal), new HashMap<>(reflUsed));
    }

    /**
     * Goes back to an earlier state: the proof loses the steps added since then (it is rebuilt from its premises by
     * replaying the steps it keeps, so a step that a later rule discharged is valid again) and the goals and the
     * applied rules are the ones of then.
     *
     * @param state a state of the current proof, taken with {@link #state()}
     */
    protected final void restore(State state) {
        List<AbstractModalAction> actions = proof.parse();
        proof.reset();
        for (int i = proof.getSteps().size(); i < state.steps(); i++) {
            actions.get(i).apply(proof);
        }
        goals = new ArrayList<>(state.goals());
        actionsDone = new ArrayList<>(state.actionsDone());
        usedForGoal = new HashMap<>(state.usedForGoal());
        reflUsed = new HashMap<>(state.reflUsed());
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
            var intro = introRuleForGoal();
            if (intro.isPresent() || isGoalReached()) {
                updateGoals(intro.orElse(null));
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
        var lastGoal = goals.getLast();
        return !proof.getSteps().isEmpty() &&
                lastGoal.getValue().equals(proof.getSteps().getLast().getStep())
                && lastGoal.getKey().equals(proof.getSteps().getLast().getState());
    }

    /**
     * Look for an elimination rule, and apply it.
     *
     * @return true iff a rule was found and was applied
     */
    private boolean eliminateRules() {
        return lookForElimRules()
                .map(c -> {
                    c.apply(proof);
                    return true;
                }).orElse(false);
    }

    /**
     * Apply the rule that attains the next goal
     *
     * @param intro action that will attain the goal
     */
    private void updateGoals(AbstractModalAction intro) {
        if (intro != null) {
            intro.apply(proof);
        }
        attainFalseGoal();
        goals.removeLast();
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
        if (goals.size() > 1 && goals.getLast().getValue().equals(ConstantModal.FALSE)
                && goals.get(goals.size() - 2).getValue() instanceof NegationModal) {
            ModalNotI cla = new ModalNotI();
            if (cla.isValid(proof)) {
                cla.apply(proof);
                goals.removeLast();
            }
        }
    }

    /**
     * Check if last goal can be reached with an intro rule
     *
     * @return the action that must be applied to reach the goal
     */
    private Optional<AbstractModalAction> introRuleForGoal() {
        if (proof.getSteps().isEmpty()) {
            return Optional.empty();
        }
        ModalOperation goal = goals.getLast().getValue();
        String state = goals.getLast().getKey();
        Optional<AbstractModalAction> copy = copyOfGoal(goal, state);
        if (copy.isPresent()) {
            return copy;
        }
        Optional<AbstractModalAction> sol = Optional.empty();
        if (goal instanceof ConjunctionModal conj) {
            sol = introRuleForGoalConjuntion(conj, state);
        } else if (goal instanceof ImplicationModal imp) {
            sol = introRuleForGoalImplication(imp, state);
        } else if (goal instanceof DisjunctionModal disj) {
            sol = introRuleForGoalDisjunction(disj, state);
        } else if (goal instanceof NegationModal neg) {
            sol = introRuleForGoalNegation(neg, state);
        } else if (goal.equals(ConstantModal.FALSE)) {
            sol = introRuleForGoalContradiction();
        } else if (goal instanceof Always always) {
            sol = introRuleForGoalAlways(always, state);
        } else if (goal instanceof Sometime sometime) {
            sol = introRuleForGoalSometime(sometime, state);
        }
        return sol.or(() -> introRuleForOtherGoal(goal, state));
    }

    /**
     * A repetition of an earlier line that already is the goal in its state, unless that would only repeat the last
     * line.
     */
    private Optional<AbstractModalAction> copyOfGoal(ModalOperation goal, String state) {
        for (int i = 0; i < proof.getSteps().size()-1; i++) {
            if (proof.getSteps().get(i).isValid() &&
                    goal.equals(proof.getSteps().get(i).getStep()) &&
                    state.equals(proof.getSteps().get(i).getState()) &&
                    (!goal.equals(proof.getSteps().getLast().getStep()) ||
                    !(goal.equals(ConstantModal.FALSE) ||
                            state.equals(proof.getSteps().getLast().getState())))) {
                return Optional.of(new ModalCopy(i + 1));
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> introRuleForGoalSometime(Sometime sometime, String state) {
        ModalLogicalOperation element = sometime.getElement();
        Map<String, Integer> states = new HashMap<>();
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(element)) {
                states.put(proof.getSteps().get(k).getState(), k+1);
            }
        }
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep() instanceof LessEqual lessEqual
                    && lessEqual.getLeft().equals(state)) {
                Integer finalState = states.get(lessEqual.getRight());
                if (finalState != null) {
                    return Optional.of(new ModalDiaI(finalState, k+1));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> introRuleForGoalAlways(Always goal, String state) {
        ModalLogicalOperation element = goal.getElement();
        int i = 0;
        int assmsLevel = proof.getSteps().getLast().getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep() instanceof LessEqual lessEqual && lessEqual.getLeft().equals(state)) {
            var finalState = lessEqual.getRight();
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(element) && proof.getSteps().get(k).getState().equals(finalState)) {
                    if (k + 1 < proof.getSteps().size() && !proof.getSteps().getLast().getStep().equals(proof.getSteps().get(k).getStep())) {
                        (new ModalCopy(k + 1)).apply(proof);
                    }
                    return Optional.of(new ModalBoxI());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ConstantModal#FALSE}
     *
     * @return the action {@link ModalFI} that must be used, if possible
     */
    private Optional<AbstractModalAction> introRuleForGoalContradiction() {
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep() instanceof NegationModal neg) {
                ModalLogicalOperation element = neg.getElement();
                var state = proof.getSteps().get(i).getState();
                for (int j = 0; j < proof.getSteps().size(); j++) {
                    if (proof.getSteps().get(j).isValid() && proof.getSteps().get(j).getStep().equals(element) && proof.getSteps().get(j).getState().equals(state)) {
                        return Optional.of(new ModalFI(j + 1, i + 1));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link NegationModal}
     *
     * @return the action {@link ModalNotI} that must be used, if possible
     */
    private Optional<AbstractModalAction> introRuleForGoalNegation(NegationModal goal, String state) {
        ModalOperation element = goal.getElement();
        int i = 0;
        int assmsLevel = proof.getSteps().getLast().getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep().equals(element) && proof.getSteps().get(proof.getSteps().size() - i).getState().equals(state)) {
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(ConstantModal.FALSE)) {
                    if (k + 1 < proof.getSteps().size()) {
                        (new ModalCopy(k + 1)).apply(proof);
                    }
                    return Optional.of(new ModalNotI());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link DisjunctionModal}
     *
     * @return the action {@link ModalOrI1} or {@link ModalOrI2} that must be used, if possible
     */
    private Optional<AbstractModalAction> introRuleForGoalDisjunction(DisjunctionModal goal, String state) {
        ModalLogicalOperation left = goal.getLeft();
        ModalLogicalOperation right = goal.getRight();
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid()) {
                if (proof.getSteps().get(k).getStep().equals(left) && proof.getSteps().get(k).getState().equals(state)) {
                    return Optional.of(new ModalOrI1(k + 1, right));
                }
                if (proof.getSteps().get(k).getStep().equals(right) && proof.getSteps().get(k).getState().equals(state)) {
                    return Optional.of(new ModalOrI2(k + 1, left));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ConjunctionModal}
     *
     * @return the action {@link ModalAndI} that must be used, if possible
     */
    private Optional<AbstractModalAction> introRuleForGoalConjuntion(ConjunctionModal goal, String state) {
        ModalLogicalOperation left = goal.getLeft();
        int a = 0;
        ModalLogicalOperation right = goal.getRight();
        int b = 0;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid()) {
                if (left.equals(proof.getSteps().get(i).getStep()) && state.equals(proof.getSteps().get(i).getState())) {
                    a = i + 1;
                }
                if (right.equals(proof.getSteps().get(i).getStep()) && state.equals(proof.getSteps().get(i).getState())) {
                    b = i + 1;
                }
            }
            if (a > 0 && b > 0) {
                return Optional.of(new ModalAndI(a, b));
            }
        }
        return Optional.empty();
    }

    /**
     * Check if last goal can be reached with an intro rule in case it is {@link ImplicationModal}
     *
     * @return the action {@link ModalDeductionTheorem} that must be used, if possible
     */
    private Optional<AbstractModalAction> introRuleForGoalImplication(ImplicationModal goal, String state) {
        ModalLogicalOperation left = (goal).getLeft();
        ModalLogicalOperation right = (goal).getRight();
        int i = 0;
        int assmsLevel = proof.getSteps().getLast().getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep().equals(left) && proof.getSteps().get(proof.getSteps().size() - i).getState().equals(state)) {
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(right) && proof.getSteps().get(k).getState().equals(state)) {
                    if (k + 1 < proof.getSteps().size() && !proof.getSteps().getLast().getStep().equals(proof.getSteps().get(k).getStep())) {
                        (new ModalCopy(k + 1)).apply(proof);
                    }
                    return Optional.of(new ModalDeductionTheorem());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> lookForElimRules() {
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep() instanceof ModalLogicalOperation) {
                int k = i + 1;
                var act = Optional.of(new ModalDeMorgan(k))
                        .flatMap(this::checkSingleAction)
                        .or(() -> Optional.of(new DeMorgan(k))
                                .flatMap(this::checkSingleAction))
                        .or(() -> Optional.of(new ModalNotE(k))
                                .flatMap(this::checkSingleAction))
                        .or(() -> Optional.of(new Reflexive(k))
                                .flatMap(this::checkReflAction))
                        .or(() -> checkAdditionOfAndI(k - 1))
                        .or(() -> checkAdditionOfDisjIModPonens(k - 1))
                        .or(() -> checkAdditionOfAlways(k - 1))
                        .or(() -> checkAdditionOfContraAlw(k - 1))
                        .or(() -> elimRuleForOtherStep(k - 1));
                if (act.isPresent()) {
                    return act;
                }
            } else if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep() instanceof RelationOperation){
                var act = checkAdditionOfTrans(i);
                if (act.isPresent()){
                    return act;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> checkAdditionOfContraAlw(int i) {
        for (int j = 0; j < proof.getSteps().size(); j++) {
            if (proof.getSteps().get(j).isValid()) {
                var opt = Optional.of(new ContraSometime(i + 1, j + 1))
                        .flatMap(this::checkSingleAction);
                if (opt.isPresent()) {
                    return opt;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> checkAdditionOfTrans(int i) {
        if (proof.getSteps().get(i).getStep() instanceof LessEqual lessEqual && !lessEqual.getLeft().equals(lessEqual.getRight())) {
            for (int j = 0; j < proof.getSteps().size(); j++) {
                if (proof.getSteps().get(j).isValid() && proof.getSteps().get(j).getStep() instanceof LessEqual lessEqual1 && !lessEqual1.getLeft().equals(lessEqual1.getRight())) {
                    var opt = Optional.of(new Transitive(i + 1, j + 1))
                            .flatMap(this::checkSingleAction);
                    if (opt.isPresent()) {
                        return opt;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> checkAdditionOfAlways(int i) {
        for (int j = 0; j < proof.getSteps().size(); j++) {
            if (proof.getSteps().get(j).isValid()) {
                var opt = Optional.of(new ModalBoxE(i + 1, j + 1))
                        .flatMap(this::checkSingleAction);
                if (opt.isPresent()) {
                    return opt;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> checkAdditionOfAndI(int i) {
        return Optional.of(new ModalAndE1(i + 1))
                .flatMap(this::checkSingleAction)
                .or(() -> Optional.of(new ModalAndE2(i + 1))
                        .flatMap(this::checkSingleAction));
    }

    /**
     * Check that the action has not already done and it is valid
     *
     * @param act action to be checked for validity
     * @return the action if it valid
     */
    protected final Optional<AbstractModalAction> checkSingleAction(AbstractModalAction act) {
        var answer = Optional.ofNullable(act)
                .filter(a -> !actionsDone.contains(a) && a.isValid(proof) && isUsefulElimination(a));
        answer.ifPresent(a -> actionsDone.add(a));
        return answer;
    }

    /**
     * {@code Refl} on a state is applied once, and again only when the {@code s <= s} step it gave was discharged.
     * {@link #reflUsed} maps the state to the 0-based index of that step, which is the size of the proof before it is
     * applied.
     */
    private Optional<AbstractModalAction> checkReflAction(Reflexive act) {
        int k = act.getStep();
        var state = proof.getSteps().get(k - 1).getState();
        Integer used = reflUsed.get(state);
        if (used != null && used < proof.getSteps().size() && proof.getSteps().get(used).isValid()) {
            return Optional.empty();
        }
        reflUsed.put(state, proof.getSteps().size());
        return Optional.of(act);
    }

    private Optional<AbstractModalAction> checkAdditionOfDisjIModPonens(int i) {
        for (int j = 0; j < proof.getSteps().size(); j++) {
            if (proof.getSteps().get(j).isValid()) {
                int k = j + 1;
                var act = Optional.of(new ModalModusPonens(i + 1, k))
                        .flatMap(this::checkSingleAction)
                        .or(() -> Optional.of(new ModalOrE1(i + 1, k))
                                .flatMap(this::checkSingleAction))
                        .or(() -> Optional.of(new ModalOrE2(i + 1, k))
                                .flatMap(this::checkSingleAction));
                if (act.isPresent()) {
                    return act;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Add a new goal to make easier to reach the current goal, if possible
     */
    private void updateGoal() {
        if (goals.getLast().getValue().equals(ConstantModal.FALSE)) {
            lastGoalFalse();
        } else if (goals.getLast().getValue() instanceof ModalLogicalOperation goal) {
            // A relation goal (s0 <= s1) has no subgoals: only the premises and the elimination rules can reach it
            var state = goals.getLast().getKey();
            switch (goal) {
                case ConjunctionModal conj -> updateGoalConjunction(conj, state);
                case ImplicationModal imp -> updateGoalImplication(imp, state);
                case NegationModal neg -> updateGoalNegation(neg, state);
                case Always always -> updateGoalAlways(always, state);
                case null, default -> {
                    if (!updateOtherGoal(goal, state)) {
                        updateGoalContradiction(goal, state);
                    }
                }
            }
        }
    }

    /**
     * Update the goal list in case the last goal is {@link ConstantModal#FALSE}
     */
    private void lastGoalFalse() {
        int j = -1;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            boolean b = true;
            if (!usedForGoal.containsValue(i) && proof.getSteps().get(i).isValid()) {
                String state = proof.getSteps().get(i).getState();
                LogicOperation log = proof.getSteps().get(i).getStep();
                switch (log) {
                    case NegationModal neg -> goals.add(new AbstractMap.SimpleEntry<>(state, neg.getElement()));
                    case DisjunctionModal disj ->
                            goals.add(new AbstractMap.SimpleEntry<>(state, new NegationModal(disj.getLeft())));
                    case ImplicationModal imp -> goals.add(new AbstractMap.SimpleEntry<>(state, imp.getLeft()));
                    case Sometime some ->
                            goals.add(new AbstractMap.SimpleEntry<>(state, new Always(new NegationModal(some.getElement()))));
                    case null, default -> b = false;
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
     * Create new goal a assumption from the last goal. Assume the opposite
     * and try to reach a contradiction
     *
     * @param goal last goal
     */
    private void updateGoalContradiction(ModalLogicalOperation goal, String state) {
        var neg = new NegationModal(goal);
        boolean alreadyExists = false;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep().equals(neg)) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            goals.add(new AbstractMap.SimpleEntry<>(state, new NegationModal(new NegationModal(goal))));
            goals.add(new AbstractMap.SimpleEntry<>(state, ConstantModal.FALSE));
            (new ModalAssume(neg, state)).apply(proof);
        }
    }

    /**
     * Create new goal and assumption from the last goal of type -
     *
     * @param goal last goal of type -
     */
    private void updateGoalNegation(NegationModal goal, String state) {
        goals.add(new AbstractMap.SimpleEntry<>(state, ConstantModal.FALSE));
        (new ModalAssume(goal.getElement(), state)).apply(proof);
    }

    /**
     * Create new goal and assumption from the last goal of type ->
     *
     * @param goal last goal of type ->
     */
    private void updateGoalImplication(ImplicationModal goal, String state) {
        goals.add(new AbstractMap.SimpleEntry<>(state, goal.getRight()));
        (new ModalAssume(goal.getLeft(), state)).apply(proof);
    }

    /**
     * Create new goals from the last goal of type AND
     *
     * @param goal last goal of type AND
     */
    private void updateGoalConjunction(ConjunctionModal goal, String state) {
        goals.add(new AbstractMap.SimpleEntry<>(state, goal.getLeft()));
        goals.add(new AbstractMap.SimpleEntry<>(state, goal.getRight()));
    }

    /**
     * Create new goals from the last goal of type []
     *
     * @param goal last goal of type []
     */
    private void updateGoalAlways(Always goal, String state) {
        var newState = proof.newState();
        goals.add(new AbstractMap.SimpleEntry<>(newState, goal.getElement()));
        (new ModalAssume(new LessEqual(state, newState))).apply(proof);
    }
}