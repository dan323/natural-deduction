package com.dan323.uses;

import com.dan323.model.ProofDto;

public class LogicalSolver implements ActionsUseCases.Solve {

    private final Transformer transformer;

    public LogicalSolver(Transformer transformer){
        this.transformer = transformer;
    }

    public ProofDto perform(ProofDto proof) {
        var naturalDeduction = transformer.from(proof);
        naturalDeduction.automate();
        return transformer.fromProof(naturalDeduction);
    }
}
