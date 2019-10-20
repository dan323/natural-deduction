package com.dan323.proof.generic;

import com.dan323.expresions.base.Implication;
import com.dan323.proof.generic.proof.Proof;

/**
 * @author danco
 */
public final class RuleUtils {

    private RuleUtils() {
    }

    public static boolean isValidModusPonens(Proof pf, int applyAt1, int applyAt2) {
        return isValidIndexAndProp(pf, applyAt1) &&  // applyAt1 is an valid index
                // applyAt1 gives result to an active proposition
                isValidIndexAndProp(pf, applyAt2) && // applyAt2 is a valid index
                // applyAt2 gives result to an active proposition
                isOperation(pf, applyAt1, Implication.class) && // applyAt2 is an implication
                ((Implication) pf.getSteps().get(applyAt1 - 1).getStep()).getLeft().equals(pf.getSteps().get(applyAt2 - 1).getStep()); // applyAt2 -> something == applyAt1
    }

    public static boolean isValidIndex(Proof pf, int index) {
        return 1 <= index && pf.getSteps().size() >= index;
    }

    public static boolean isValidPropAtIndex(Proof pf, int index) {
        return pf.getSteps().get(index - 1).isValid();
    }

    public static boolean isValidIndexAndProp(Proof pf, int index) {
        return isValidIndex(pf, index) && isValidPropAtIndex(pf, index);
    }

    public static boolean isOperation(Proof pf, int index, Class c) {
        return c.isInstance(pf.getSteps().get(index - 1).getStep());
    }

}
