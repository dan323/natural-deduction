package com.dan323.uses.intuitionistic;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.uses.classical.ParseClassicalProof;

/**
 * Parses a proof text file of intuitionistic logic: a classical proof that must not use double negation elimination.
 */
public class ParseIntuitionisticProof extends ParseClassicalProof {

    @Override
    public String logic() {
        return IntuitionisticRules.LOGIC;
    }

    @Override
    public NaturalDeduction parseProof(String proof) {
        var naturalDeduction = super.parseProof(proof);
        IntuitionisticRules.checkProof(naturalDeduction);
        return naturalDeduction;
    }
}
