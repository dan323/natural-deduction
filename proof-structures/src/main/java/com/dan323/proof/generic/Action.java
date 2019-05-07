package com.dan323.proof.generic;

import com.dan323.proof.Proof;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Action extends AbstractAction{

    void apply(Proof pf);

    static int getLastAssumptionLevel(Proof pf){
        return pf.getSteps().get(pf.getSteps().size()-1).getAssumptionLevel();
    }

    static int disableUntilLastAssumption(Proof pf,int assLevel){
        return getToLastAssumptionWhileComplex(pf,assLevel, proof->(ass-> (x->disable(proof,ass,x))));
    }

    private static void disable(Proof pf,int assLevel, int i){
        if (pf.getSteps().get(pf.getSteps().size()-i).getAssumptionLevel()==assLevel){
            pf.getSteps().get(pf.getSteps().size()-i).disable();
        }
    }

    /**
     *
     * @param pf proof
     * @param assLevel assumption level
     * @param complex given the proof, the assumption level and the number of a line in the proof, it does an action.
     * @return the index of the last assumption made
     */
    static int getToLastAssumptionWhileComplex(Proof pf, int assLevel, Function<Proof, Function<Integer, Consumer<Integer>>> complex){
        int i=1;
        while (i<=pf.getSteps().size() && pf.getSteps().get(pf.getSteps().size()-i).getAssumptionLevel()>=assLevel){
            complex.apply(pf).apply(assLevel).accept(i);
            i++;
        }
        return i-1;
    }

    static int getToLastAssumption(Proof pf,int assLevel){
        return getToLastAssumptionWhileComplex(pf,assLevel,proof -> (ass->(x-> {})));
    }
}
