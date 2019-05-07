package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Negation;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class NotE implements AbstractAction {

    private final int neg;

    public NotE(int i){
        neg=i;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf,neg)){
            LogicOperation lo=pf.getSteps().get(neg-1).getStep();
            return (lo instanceof Negation) && (((Negation)lo).getElement() instanceof Negation);
        }
        return false;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(neg);
        LogicOperation sol = ((Negation) ((Negation) (pf.getSteps().get(neg - 1).getStep())).getElement()).getElement();
        pf.getSteps().add(supp.generateProofStep(assLevel,sol,new ProofReason("-E", lst)));
    }

    protected int getNeg(){
        return neg;
    }
}
