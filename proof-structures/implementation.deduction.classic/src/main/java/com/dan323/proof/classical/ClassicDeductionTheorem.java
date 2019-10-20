package com.dan323.proof.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicDeductionTheorem extends DeductionTheorem implements ClassicalAction {

    public ClassicDeductionTheorem() {
        super((l1, l2) -> new ImplicationClassic((ClassicalLogicOperation) l1, (ClassicalLogicOperation) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ProofStep::new);
    }

    @Override
    public boolean equals(Object ob) {
        return ob instanceof ClassicDeductionTheorem;
    }

    @Override
    public int hashCode() {
        return 21 * super.hashCode() * 19;
    }
}
