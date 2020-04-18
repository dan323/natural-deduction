package com.dan323.classical.complex;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.ClassicalAction;

import java.util.List;

public final class Sequence extends CompositionRule {

    private List<ClassicalAction> lst;

    public Sequence(List<ClassicalAction> l) {
        lst = l;
    }

    @Override
    public boolean isValid(NaturalDeduction pf) {
        int size = pf.getSteps().size();
        for (ClassicalAction ca : lst) {
            if (!ca.isValid(pf)) {
                resetProof(pf, size);
                return false;
            }
            ca.apply(pf);
        }
        resetProof(pf, size);
        return true;
    }

    private void resetProof(NaturalDeduction pf, int size) {
        List<ClassicalAction> actions = pf.getParser().translateToActions(pf);
        pf.initializeProof(pf.getAssms(), pf.getGoal());
        for (int i = pf.getAssms().size(); i < size; i++) {
            actions.get(i).apply(pf);
        }
    }

    @Override
    public void apply(NaturalDeduction pf) {
        for (ClassicalAction ca : lst) {
            ca.apply(pf);
        }
    }
}
