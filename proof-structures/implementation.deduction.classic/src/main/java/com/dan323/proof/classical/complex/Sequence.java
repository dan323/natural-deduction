package com.dan323.proof.classical.complex;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.proof.classical.ClassicalAction;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public final class Sequence extends CompositionRule {

    private List<ClassicalAction> lst;

    public Sequence(List<ClassicalAction> l) {
        lst = l;
    }

    @Override
    public boolean isValid(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        int size = pf.getSteps().size();
        for (ClassicalAction ca : lst) {
            if (!ca.isValid(pf)) {
                return false;
            }
            ca.apply(pf);
        }
        int size2 = pf.getSteps().size();
        for (int i = size; i < size2; i++) {
            pf.getSteps().remove(pf.getSteps().size() - 1);
        }
        return true;
    }

    @Override
    public void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        for (ClassicalAction ca : lst) {
            ca.apply(pf);
        }
    }
}
