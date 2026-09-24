package com.dan323.uses.modal.nextuntil;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.nextuntil.ModalNextUntilNaturalDeduction;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.modal.ModalProofParser;

/**
 * Parses a proof text file of {@code modal-next-until}: the modal layout ({@code s0+1:    p           XE [1]}), with
 * {@code X A} and {@code A U B} formulas, successor states and the Next and Until rules.
 * <p>
 * As in the other logics, the last line is the goal and the proof must prove it. Here that means proving it in
 * {@code s0}: a file that ends with {@code s0+1: p} does not prove {@code p}, so it is rejected instead of being
 * answered as a proof whose {@code done} (which looks at the formula only) would disagree with the domain's.
 */
public class ModalNextUntilProofParser extends ModalProofParser {

    @Override
    public String logic() {
        return ModalNextUntilConfiguration.LOGIC;
    }

    /**
     * @throws InvalidProofException also when the proof does not prove its goal (the last line) in the initial state
     */
    @Override
    public ModalNaturalDeduction parseProof(String proof) {
        var nd = super.parseProof(proof);
        if (!nd.isDone()) {
            throw new InvalidProofException("The proof is invalid: it proves its goal in " + nd.getSteps().getLast().getState()
                    + ", not in the initial state " + nd.getState0());
        }
        return nd;
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
