package com.dan323.classical;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.proof.ProofStep;

public final class ClassicDeductionTheorem extends DeductionTheorem<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicDeductionTheorem() {
        super(ImplicationClassic::new);
    }

}
