package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.ProofParser;

public class ParseClassicalProof implements ProofParser<NaturalDeduction, ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, ClassicalAction> {

    public String logic() {
        return "classical";
    }

    @Override
    public NaturalDeduction getNewProof() {
        return new NaturalDeduction();
    }

    public ProofStep<ClassicalLogicOperation> parseLine(String line) {
        var parts = ProofLine.split(line);
        var reason = ParseClassicalAction.parseReason(parts.rule());
        if (reason == null) {
            throw new InvalidProofException("unknown rule '" + parts.rule() + "'");
        }
        return new ProofStep<>(parts.assmsLevel(), ParseClassicalAction.parseExpression(parts.expression()), reason);
    }

}
