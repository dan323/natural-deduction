package com.dan323.uses.modal.nextuntil;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.nextuntil.ModalNextUntilNaturalDeduction;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.uses.modal.ModalProofParser;

/**
 * Parses a proof text file of {@code modal-next-until}: the modal layout ({@code s0+1:    p           XE [1]}), with
 * {@code X A} and {@code A U B} formulas, successor states and the Next and Until rules.
 */
public class ModalNextUntilProofParser extends ModalProofParser {

    @Override
    public String logic() {
        return ModalNextUntilConfiguration.LOGIC;
    }

    @Override
    public ModalNaturalDeduction getNewProof() {
        return new ModalNextUntilNaturalDeduction("s0");
    }

    @Override
    protected ModalOperation parseExpression(String expression) {
        return ParseModalNextUntilAction.parseExpression(expression);
    }

    @Override
    protected ProofReason parseReason(String rule) {
        return ParseModalNextUntilAction.parseReason(rule);
    }

    @Override
    protected String parseState(String state) {
        return StateTerm.normalize(state);
    }
}
