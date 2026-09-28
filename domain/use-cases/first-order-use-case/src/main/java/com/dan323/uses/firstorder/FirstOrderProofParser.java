package com.dan323.uses.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.FirstOrderAction;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ProofParser;

/**
 * Parses a first-order proof text file. It has the usual layout, with no state prefix
 * ({@code    P(a)           ∀E [1]}); the rules are those {@link ParseFirstOrderAction#parseReason} reads, so
 * {@code =I} has no lines and the term of a {@code ∀E} step is not written down.
 */
public class FirstOrderProofParser implements ProofParser<FirstOrderNaturalDeduction, FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderAction> {

    @Override
    public String logic() {
        return FirstOrderConfiguration.LOGIC;
    }

    @Override
    public FirstOrderNaturalDeduction getNewProof() {
        return new FirstOrderNaturalDeduction();
    }

    @Override
    public ProofStep<FirstOrderOperation> parseLine(String line) {
        var parts = ProofLine.split(line);
        var reason = ParseFirstOrderAction.parseReason(parts.rule());
        return new ProofStep<>(parts.assmsLevel(), ParseFirstOrderAction.parseExpression(parts.expression()), reason);
    }
}
