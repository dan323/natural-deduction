package com.dan323.uses.classical;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.model.ProofDto;
import com.dan323.uses.LogicalSolver;

public class ClassicSolver implements LogicalSolver {

    public ClassicSolver() {
    }

    @Override
    public ProofDto perform(ProofDto proof) {
        var transformer = new ClassicalProofTransformer();
        NaturalDeduction naturalDeduction = transformer.from(proof);
        naturalDeduction.automate();
        return transformer.fromProof(naturalDeduction);
    }

    @Override
    public String getLogicName() {
        return "classical";
    }
}
