package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class of any action taken in the proof in a classical setting
 */
public interface Action<T extends LogicOperation, Q extends ProofStep<T>> extends AbstractAction<T, Q> {

    static <T extends LogicOperation, Q extends ProofStep<T>> int getLastAssumptionLevel(Proof<T, Q> pf) {
        return pf.getSteps().isEmpty() ? 0 : pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
    }

    static <T extends LogicOperation, Q extends ProofStep<T>> int disableUntilLastAssumption(Proof<T, Q> pf, int assLevel) {
        return getToLastAssumptionWhileComplex(pf, assLevel, proof -> (ass -> (x -> disable(proof, ass, x))));
    }

    private static <T extends LogicOperation, Q extends ProofStep<T>> void disable(Proof<T, Q> pf, int assLevel, int i) {
        if (pf.getSteps().get(pf.getSteps().size() - i).getAssumptionLevel() == assLevel) {
            pf.getSteps().get(pf.getSteps().size() - i).disable();
        }
    }

    /**
     * @param <Q>      type of the step in the proof
     * @param <T>      type of the logic operation
     * @param pf       proof
     * @param assLevel assumption level
     * @param complex  given the proof, the assumption level and the number of a line in the proof, it does an action.
     * @return the index of the last assumption made
     */
    static <T extends LogicOperation, Q extends ProofStep<T>> int getToLastAssumptionWhileComplex(Proof<T, Q> pf, int assLevel, Function<Proof<T, Q>, Function<Integer, Consumer<Integer>>> complex) {
        int i = 1;
        while (i <= pf.getSteps().size() && pf.getSteps().get(pf.getSteps().size() - i).getAssumptionLevel() >= assLevel) {
            complex.apply(pf).apply(assLevel).accept(i);
            i++;
        }
        return i - 1;
    }

    static <T extends LogicOperation, Q extends ProofStep<T>> int getToLastAssumption(Proof<T, Q> pf, int assLevel) {
        return getToLastAssumptionWhileComplex(pf, assLevel, proof -> (ass -> (x -> {
        })));
    }

    void apply(Proof<T, Q> pf);
}
