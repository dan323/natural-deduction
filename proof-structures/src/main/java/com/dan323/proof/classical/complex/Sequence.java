package com.dan323.proof.classical.complex;

import com.dan323.proof.Proof;
import com.dan323.proof.classical.ClassicalAction;
import com.dan323.proof.generic.ProofStepSupplier;

import java.util.List;

public class Sequence implements ClassicalAction {

    List<ClassicalAction> lst;

    public Sequence(List<ClassicalAction> l) {
        lst = l;
    }

    @Override
    public boolean isValid(Proof pf) {
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
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        //Unnecessary method
    }

    @Override
    public void apply(Proof pf) {
        for (ClassicalAction ca : lst) {
            ca.apply(pf);
        }
    }
}
