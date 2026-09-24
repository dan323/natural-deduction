package com.dan323.uses.modal.until;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.until.ParseModalUntilAction;
import com.dan323.uses.modal.ModalProofTransformer;

import java.util.List;

/**
 * Reads and writes proofs of modal logic with Until: modal proofs whose formulas may use {@code A U B} and whose steps
 * may use the Until rules.
 * <p>
 * The solver is the modal one ({@link ModalNaturalDeduction#automate()}): every rule it uses is a rule of this logic,
 * so what it proves is right, but it reads {@code A U B} as if it were a variable and never uses an Until rule. It
 * does not prove {@code q ⊢ p U q}; it answers with an unfinished proof.
 */
public class ModalUntilProofTransformer extends ModalProofTransformer {

    private static final List<ActionDescriptorDto> ACTIONS = new ModalUntilGetActions().perform();

    @Override
    public String logic() {
        return ModalUntilConfiguration.LOGIC;
    }

    @Override
    protected List<ActionDescriptorDto> actions() {
        return ACTIONS;
    }

    @Override
    protected ModalNaturalDeduction newProof() {
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

    @Override
    protected AbstractModalAction parseWithReason(ModalNaturalDeduction proof, ModalOperation step, ProofReason reason, String state) {
        return ParseModalUntilAction.parseWithReason(proof, step, reason, state);
    }

    @Override
    protected AbstractModalAction parseAction(String name, List<Integer> sources, ModalOperation expression, String state) {
        return ParseModalUntilAction.parseAction(name, sources, expression, state);
    }
}
