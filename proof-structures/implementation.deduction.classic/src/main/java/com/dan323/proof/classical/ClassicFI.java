package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConstantClassic;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.FI;

public final class ClassicFI extends FI implements ClassicalAction {

    public ClassicFI(int i, int j) {
        super(i, j, lo -> new NegationClassic((ClassicalLogicOperation) lo), () -> ConstantClassic.FALSE);
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
