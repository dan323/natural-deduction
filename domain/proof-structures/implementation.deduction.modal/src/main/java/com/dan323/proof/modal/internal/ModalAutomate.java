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

/**
 * Class to execute a Natural deduction in modal logic
 *
 * @author daniel
 */
public final class ModalAutomate {

    private ModalNaturalDeduction proof;
    private List<Map.Entry<String, ModalOperation>> goals;
    private List<AbstractModalAction> actionsDone;
    private Map<Integer, Integer> usedForGoal;

    private Map<String, Integer> reflUsed;

    private ModalAutomate() {
    }

    public static final ModalAutomate AUTOMATIC_SOLVER = new ModalAutomate();

    /**
     * Finish the proof if it can be done.
     * It will stop without solving it if it cannot be solved
     *
     * @param naturalDeduction the proof to solve
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

        var c = true;
        while (c) {
            var goalSize = goals.size();
            var stepsSize = proof.getSteps().size();
            c = applyIntroAndElimRules();
            if (c) {
                updateGoal();
                // If the state of the proof has not changed, stop. It has failed
                c = isStateChanged(goalSize, stepsSize);
            }
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
            var intro = introRuleForGoal();
            if (intro.isPresent() || isGoalReached()) {
                updateGoals(intro.orElse(null));
                if (goals.isEmpty()) {
                    c = false;
                    b = false;
                }
            } else {
                b = eliminateRules();
            }
            System.out.println("Start:\n" + proof.toString() + "\n");
        }
        return c;
    }

    /**
     * Check if the goal was reabhed
     *
     * @return true if the last goal is equal to the last statement in the proof
     */
    private boolean isGoalReached() {
        var lastGoal = goals.get(goals.size() - 1);
        return !proof.getSteps().isEmpty() &&
                lastGoal.getValue().equals(proof.getSteps().get(proof.getSteps().size() - 1).getStep())
                && lastGoal.getKey().equals(proof.getSteps().get(proof.getSteps().size() - 1).getState());
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
        goals.remove(goals.size() - 1);
        usedForGoal.remove(goals.size() + 2);
    }

    /**
     * If the next goal is {@literal False} and the rule NotI is valid,
     * we use it to reach the goal
     */
    private void attainFalseGoal() {
        if (goals.size() > 1 && goals.get(goals.size() - 1).getValue().equals(ConstantModal.FALSE)) {
            ModalNotI cla = new ModalNotI();
            if (cla.isValid(proof)) {
                cla.apply(proof);
                goals.remove(goals.size() - 1);
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
        ModalOperation goal = goals.get(goals.size() - 1).getValue();
        String state = goals.get(goals.size() - 1).getKey();
        for (int i = 0; i < proof.getSteps().size()-1; i++) {
            if (proof.getSteps().get(i).isValid() &&
                    goal.equals(proof.getSteps().get(i).getStep()) &&
                    state.equals(proof.getSteps().get(i).getState())) {
                return Optional.of(new ModalCopy(i + 1));
            }
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
        return sol;
    }

    private Optional<AbstractModalAction> introRuleForGoalSometime(Sometime sometime, String state) {
        ModalLogicalOperation element = sometime.getElement();
        Map<String, Integer> states = new HashMap<>();
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid()) {
                if (proof.getSteps().get(k).getStep().equals(element)) {
                    states.put(proof.getSteps().get(k).getState(), k+1);
                }
            }
        }
        for (int k = 0; k < proof.getSteps().size(); k++) {
            if (proof.getSteps().get(k).isValid()) {
                if (proof.getSteps().get(k).getStep() instanceof LessEqual lessEqual && lessEqual.getLeft().equals(state)) {
                    Integer finalState = states.get(lessEqual.getRight());
                    if (finalState != null) {
                        return Optional.of(new ModalDiaI(finalState, k+1));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<AbstractModalAction> introRuleForGoalAlways(Always goal, String state) {
        ModalLogicalOperation element = goal.getElement();
        int i = 0;
        int assmsLevel = proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep() instanceof LessEqual lessEqual && lessEqual.getLeft().equals(state)) {
            var finalState = lessEqual.getRight();
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(element) && proof.getSteps().get(k).getState().equals(finalState)) {
                    if (k + 1 < proof.getSteps().size() && !proof.getSteps().get(proof.getSteps().size() - 1).getStep().equals(proof.getSteps().get(k).getStep())) {
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
        int assmsLevel = proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
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
        int assmsLevel = proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
        while (proof.getSteps().size() - 1 - i >= 0 && proof.getSteps().get(proof.getSteps().size() - 1 - i).getAssumptionLevel() >= assmsLevel) {
            i++;
        }
        if (proof.getSteps().get(proof.getSteps().size() - i).getStep().equals(left) && proof.getSteps().get(proof.getSteps().size() - i).getState().equals(state)) {
            for (int k = 0; k < proof.getSteps().size(); k++) {
                if (proof.getSteps().get(k).isValid() && proof.getSteps().get(k).getStep().equals(right) && proof.getSteps().get(k).getState().equals(state)) {
                    if (k + 1 < proof.getSteps().size() && !proof.getSteps().get(proof.getSteps().size() - 1).getStep().equals(proof.getSteps().get(k).getStep())) {
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
                        .or(() -> checkAdditionOfContraAlw(k - 1));
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
    private Optional<AbstractModalAction> checkSingleAction(AbstractModalAction act) {
        var answer = Optional.ofNullable(act)
                .filter(a -> !actionsDone.contains(a) && a.isValid(proof));
        answer.ifPresent(a -> actionsDone.add(a));
        return answer;
    }

    private Optional<AbstractModalAction> checkReflAction(Reflexive act) {
        int k = act.getStep();
        var state = proof.getSteps().get(k - 1).getState();
        if (reflUsed.containsKey(state)) {
            if (!proof.getSteps().get(reflUsed.get(state)).isValid()) {
                reflUsed.put(state, k);
                return Optional.of(act);
            }
        } else {
            reflUsed.put(state, k);
            return Optional.of(act);
        }
        return Optional.empty();
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
        if (goals.get(goals.size() - 1).getValue().equals(ConstantModal.FALSE)) {
            lastGoalFalse();
        } else {
            var state = goals.get(goals.size() - 1).getKey();
            ModalLogicalOperation goal = (ModalLogicalOperation) goals.get(goals.size() - 1).getValue();
            if (goal instanceof ConjunctionModal conj) {
                updateGoalConjunction(conj, state);
            } else if (goal instanceof ImplicationModal imp) {
                updateGoalImplication(imp, state);
            } else if (goal instanceof NegationModal neg) {
                updateGoalNegation(neg, state);
            } else if (goal instanceof Always always) {
                updateGoalAlways(always, state);
            } else {
                updateGoalContradiction(goal, state);
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
                if (log instanceof NegationModal neg) {
                    goals.add(new AbstractMap.SimpleEntry<>(state, neg.getElement()));
                } else if (log instanceof DisjunctionModal disj) {
                    goals.add(new AbstractMap.SimpleEntry<>(state, new NegationModal(disj.getLeft())));
                } else if (log instanceof ImplicationModal imp) {
                    goals.add(new AbstractMap.SimpleEntry<>(state, imp.getLeft()));
                } else if (log instanceof Sometime some){
                    goals.add(new AbstractMap.SimpleEntry<>(state, new Always(new NegationModal(some.getElement())) ));
                }else {
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