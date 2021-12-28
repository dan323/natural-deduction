package com.dan323.proof.generic;

import com.dan323.expressions.base.Implication;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;

import java.util.function.IntConsumer;

/**
 * @author danco
 */
public final class RuleUtils {

    private RuleUtils() {
        throw new UnsupportedOperationException();
    }


    public static <P extends Proof<?, ?>> int getLastAssumptionLevel(P proof) {
        return proof.getSteps().isEmpty() ? 0 : proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel();
    }

    public static <P extends Proof<?, ?>> int disableUntilLastAssumption(P proof, int assLevel) {
        return getToLastAssumptionWhileComplex(proof, assLevel, x -> disable(proof, x));
    }

    private static <P extends Proof<?, ?>> void disable(P proof, int i) {
        proof.getSteps().get(proof.getSteps().size() - i).disable();
    }

    /**
     * @param <P>      type of proof
     * @param proof    proof to check
     * @param assLevel assumption level
     * @param complex  given the proof, the assumption level and the number of a line in the proof, it does an action.
     * @return the index of the last assumption made
     */
    public static <P extends Proof<?, ?>> int getToLastAssumptionWhileComplex(P proof, int assLevel, IntConsumer complex) {
        int i = 1;
        while (i <= proof.getSteps().size() && proof.getSteps().get(proof.getSteps().size() - i).getAssumptionLevel() >= assLevel) {
            complex.accept(i);
            i++;
        }
        return i - 1;
    }

    public static <P extends Proof<?, ?>> int getToLastAssumption(P proof, int assLevel) {
        return getToLastAssumptionWhileComplex(proof, assLevel, x -> {
        });
    }


    public static <P extends Proof<?, ?>> boolean isValidModusPonens(P pf, int applyAt1, int applyAt2) {
        return isValidIndexAndProp(pf, applyAt1) &&  // applyAt1 is an valid index
                // applyAt1 gives result to an active proposition
                isValidIndexAndProp(pf, applyAt2) && // applyAt2 is a valid index
                // applyAt2 gives result to an active proposition
                isOperation(pf, applyAt1, Implication.class) && // applyAt1 is an implication
                ((Implication<?>) pf.getSteps().get(applyAt1 - 1).getStep()).getLeft()
                        .equals(pf.getSteps().get(applyAt2 - 1).getStep()); // applyAt2 -> something == applyAt1
    }

    public static <P extends Proof<?, ?>> boolean isValidIndex(P pf, int index) {
        return 1 <= index && pf.getSteps().size() >= index;
    }

    public static <P extends Proof<?, ?>> boolean isValidPropAtIndex(P pf, int index) {
        return pf.getSteps().get(index - 1).isValid();
    }

    public static <P extends Proof<?, ?>> boolean isValidIndexAndProp(P pf, int index) {
        return isValidIndex(pf, index) && isValidPropAtIndex(pf, index);
    }

    public static <P extends Proof<?, ?>> boolean isOperation(P pf, int index, Class<? extends LogicOperation> c) {
        return c.isInstance(pf.getSteps().get(index - 1).getStep());
    }

}
