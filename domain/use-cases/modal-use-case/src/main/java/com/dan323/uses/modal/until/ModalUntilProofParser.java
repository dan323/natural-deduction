package com.dan323.uses.modal.until;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.until.ParseModalUntilAction;
import com.dan323.uses.modal.ModalProofParser;

/**
 * Parses a proof text file of modal logic with Until: the modal layout, with {@code A U B} formulas and the Until
 * rules ({@code UI [n]}, {@code UI [n, m]}, {@code UE [n]}, {@code UE [n, m]}).
 */
public class ModalUntilProofParser extends ModalProofParser {

    @Override
    public String logic() {
        return ModalUntilConfiguration.LOGIC;
    }

    @Override
    public ModalNaturalDeduction getNewProof() {
        return ParseModalUntilAction.newProof("s0");
    }

    @Override
    protected ModalOperation parseExpression(String expression) {
        return ParseModalUntilAction.parseExpression(expression);
    }

    @Override
    protected ProofReason parseReason(String rule) {
        return ParseModalUntilAction.parseReason(rule);
    }
}
