package com.dan323.proof.generic;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

/**
 * @author danco
 */
public final class RuleUtils {

    private RuleUtils() {
        throw new UnsupportedOperationException();
    }

    public static <T extends LogicOperation, Q extends ProofStep<T>> boolean isValidModusPonens(Proof<T, Q> pf, int applyAt1, int applyAt2) {
        return isValidIndexAndProp(pf, applyAt1) &&  // applyAt1 is an valid index
                // applyAt1 gives result to an active proposition
                isValidIndexAndProp(pf, applyAt2) && // applyAt2 is a valid index
                // applyAt2 gives result to an active proposition
                isOperation(pf, applyAt1, Implication.class) && // applyAt1 is an implication
                ((Implication) pf.getSteps().get(applyAt1 - 1).getStep()).getLeft()
                        .equals(pf.getSteps().get(applyAt2 - 1).getStep()); // applyAt2 -> something == applyAt1
    }

    public static <T extends LogicOperation, Q extends ProofStep<T>> boolean isValidIndex(Proof<T, Q> pf, int index) {
        return 1 <= index && pf.getSteps().size() >= index;
    }

    public static <T extends LogicOperation, Q extends ProofStep<T>> boolean isValidPropAtIndex(Proof<T, Q> pf, int index) {
        return pf.getSteps().get(index - 1).isValid();
    }

    public static <T extends LogicOperation, Q extends ProofStep<T>> boolean isValidIndexAndProp(Proof<T, Q> pf, int index) {
        return isValidIndex(pf, index) && isValidPropAtIndex(pf, index);
    }

    public static <T extends LogicOperation, Q extends ProofStep<T>> boolean isOperation(Proof<T, Q> pf, int index, Class<? extends LogicOperation> c) {
        return c.isInstance(pf.getSteps().get(index - 1).getStep());
    }

}
