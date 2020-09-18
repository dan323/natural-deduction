package com.dan323.classical.internal;

import com.dan323.classical.*;
import com.dan323.classical.complex.DeMorgan1;
import com.dan323.classical.complex.OrE1;
import com.dan323.classical.complex.OrE2;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.classical.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassicalAutomate {

    private NaturalDeduction proof;
    private List<ClassicalLogicOperation> goals;
    private List<ClassicalAction> actionsDone;
    private Map<Integer, Integer> usedForGoal;

    private ClassicalAutomate() {
    }

    public static final ClassicalAutomate AUTOMATIC_SOLVER = new ClassicalAutomate();

    /**
     * Finish the proof it it can be done.
     * It will stop without solving it if it cannot be solved
     *
     * @param naturalDeduction the proof to solve
     */
    public void automate(NaturalDeduction naturalDeduction) {
        // Init state
        proof = naturalDeduction;
        goals = new ArrayList<>();
        actionsDone = new ArrayList<>();
        usedForGoal = new HashMap<>();
        goals.add(proof.getGoal());

        boolean c = true;
        while (c) {
            int goalSize = goals.size();
            int stepsSize = proof.getSteps().size();
            c = applyIntroAndElimRules();
            if (c) {
                updateGoal();
                // If the state of the proof has not changed, stop. It has failed
                c = goalSize != goals.size() || stepsSize != proof.getSteps().size();
            }
        }
    }

    private boolean applyIntroAndElimRules() {
        boolean b = true;
        boolean c = true;
        while (b) {
            ClassicalAction intro = introRuleForGoal();
            if (intro != null || (!proof.getSteps().isEmpty() && goals.get(goals.size() - 1).equals(proof.getSteps().get(proof.getSteps().size() - 1).getStep()))) {
                updateGoals(intro);
                if (goals.isEmpty()) {
                    c = false;
                    b = false;
                }
            } else {
                b = eliminateRules();
            }
        }
        return c;
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

    private void updateGoals(ClassicalAction intro) {
        if (intro != null) {
            intro.apply(proof);
        }
        if (goals.size() > 1 && goals.get(goals.size() - 1).equals(ConstantClassic.FALSE)) {
            ClassicalAction cla = new ClassicNotI();
            if (cla.isValid(proof)) {
                cla.apply(proof);
                goals.remove(goals.size() - 1);
            }
        }
        goals.remove(goals.size() - 1);
        usedForGoal.remove(goals.size() + 2);
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
            if (proof.getSteps().get(i).isValid() && goal.equals(proof.getSteps().get(i).getStep()) && i + 1 < proof.getSteps().size()) {
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
        int a = -1;
        ClassicalLogicOperation right = (goal).getRight();
        int b = -1;
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
                ClassicalAction act = new DeMorgan1(i + 1);
                act = checkSingleAction(act);
                if (act != null) {
                    return act;
                }
                act = new ClassicNotE(i + 1);
                act = checkSingleAction(act);
                if (act != null) {
                    return act;
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
        if (act != null) {
            return act;
        }
        act = new ClassicAndE2(i + 1);
        act = checkSingleAction(act);
        return act;
    }

    private ClassicalAction checkSingleAction(ClassicalAction act) {
        if (!actionsDone.contains(act) && act.isValid(proof)) {
            actionsDone.add(act);
            return act;
        }
        return null;
    }

    private ClassicalAction checkAdditionOfDisjIModPonens(int i) {
        for (int j = 0; j < proof.getSteps().size(); j++) {
            if (proof.getSteps().get(j).isValid()) {
                ClassicalAction act = new ClassicModusPonens(i + 1, j + 1);
                act = checkSingleAction(act);
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
                updateGoalContradiction(goal);
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
     * Create new goal a assumption from the last goal. Assume the opposite
     * and try to reach a contradiction
     *
     * @param goal last goal
     */
    private void updateGoalContradiction(ClassicalLogicOperation goal) {
        ClassicalLogicOperation neg = new NegationClassic(goal);
        boolean alreadyExists = false;
        for (int i = 0; i < proof.getSteps().size(); i++) {
            if (proof.getSteps().get(i).isValid() && proof.getSteps().get(i).getStep().equals(neg)) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            goals.add(new NegationClassic(new NegationClassic(goal)));
            goals.add(ConstantClassic.FALSE);
            (new ClassicAssume(neg)).apply(proof);
        }
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
        goals.add((goal).getLeft());
        goals.add((goal).getRight());
    }
}
