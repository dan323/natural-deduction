package com.dan323.uses.intuitionistic;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.uses.classical.ClassicalProofTransformer;

/**
 * Reads and writes intuitionistic proofs: classical proofs without double negation elimination. A proof that uses it
 * is an invalid proof and an action that names it an invalid action. The classical solver may use that rule, so this
 * logic has no solver.
 */
public class IntuitionisticProofTransformer extends ClassicalProofTransformer {

    @Override
    public String logic() {
        return IntuitionisticRules.LOGIC;
    }

    @Override
    public NaturalDeduction from(ProofDto proof) {
        var naturalDeduction = super.from(proof);
        IntuitionisticRules.checkProof(naturalDeduction);
        return naturalDeduction;
    }

    @Override
    public ClassicalAction from(ActionDto action) {
        IntuitionisticRules.checkActionName(action.name(), constantName(action.name()));
        return super.from(action);
    }

    @Override
    public boolean hasSolver() {
        return false;
    }
}
