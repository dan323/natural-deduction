package com.dan323.proof.classical;

import com.dan323.expresions.clasical.ClassicalLogicOperation;
import com.dan323.expresions.clasical.ImplicationClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofStep;
import com.dan323.proof.generic.DeductionTheorem;

public class ClassicDeductionTheorem extends DeductionTheorem implements ClassicalAction {

    public ClassicDeductionTheorem() {
        super((l1, l2) -> new ImplicationClassic((ClassicalLogicOperation) l1, (ClassicalLogicOperation) l2));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ProofStep::new);
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
