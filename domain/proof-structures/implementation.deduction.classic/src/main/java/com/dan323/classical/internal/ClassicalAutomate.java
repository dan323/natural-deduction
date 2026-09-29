package com.dan323.classical.internal;

import com.dan323.classical.ClassicAssume;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ConstantClassic;
import com.dan323.expressions.classical.NegationClassic;

/**
 * Class to execute a Natural deduction in classic logic: the goal-directed solver with double negation elimination,
 * which reaches any other goal by contradiction.
 *
 * @author daniel
 */
public final class ClassicalAutomate extends GoalDirectedAutomate {

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public ClassicalAutomate() {
        // Nothing to set up: automate initializes the state
    }

    @Override
    protected boolean eliminatesDoubleNegations() {
        return true;
    }

    /**
     * Create new goal a assumption from the last goal. Assume the opposite
     * and try to reach a contradiction
     *
     * @param goal last goal
     */
    @Override
    protected void updateOtherGoal(ClassicalLogicOperation goal) {
        ClassicalLogicOperation neg = new NegationClassic(goal);
        boolean alreadyExists = false;
        for (int i = 0; i < proof().getSteps().size(); i++) {
            if (proof().getSteps().get(i).isValid() && proof().getSteps().get(i).getStep().equals(neg)) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            pushGoal(new NegationClassic(new NegationClassic(goal)));
            pushGoal(ConstantClassic.FALSE);
            (new ClassicAssume(neg)).apply(proof());
        }
    }

    /**
     * Nothing changed, so it has failed: the proof is left as it is.
     */
    @Override
    protected boolean stalled() {
        return false;
    }
}
