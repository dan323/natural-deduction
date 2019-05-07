package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.expresions.clasical.ConstantClassic;
import com.dan323.expresions.clasical.NegationClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.FI;

public class ClassicFI extends FI implements ClassicalAction {

    public ClassicFI(int i, int j) {
        super(i, j, lo -> new NegationClassic((ClassicalLogicOperation) lo),()->new ConstantClassic(false));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
    }
}
