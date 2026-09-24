package com.dan323.uses.modal.nextuntil;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.nextuntil.ModalNextUntilNaturalDeduction;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.uses.modal.ModalProofTransformer;

import java.util.List;

/**
 * Reads and writes proofs of {@code modal-next-until}: modal proofs whose formulas may use {@code X A} and
 * {@code A U B}, whose states may be successors ({@code s0+1}) and whose steps may use the Next and Until rules. States
 * are kept the way {@link StateTerm#normalize} writes them, so {@code s0 + 1} is {@code s0+1}.
 * <p>
 * There is no solver: the modal one never uses a Next or Until rule, and it picks fresh states that know nothing of
 * successors.
 */
public class ModalNextUntilProofTransformer extends ModalProofTransformer {

    private static final List<ActionDescriptorDto> ACTIONS = new ModalNextUntilGetActions().perform();

    @Override
    public String logic() {
        return ModalNextUntilConfiguration.LOGIC;
    }

    @Override
    public boolean hasSolver() {
        return false;
    }

    @Override
    protected List<ActionDescriptorDto> actions() {
        return ACTIONS;
    }

    @Override
    protected ModalNaturalDeduction newProof() {
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
        return state == null ? null : StateTerm.normalize(state);
    }

    @Override
    protected AbstractModalAction parseWithReason(ModalNaturalDeduction proof, ModalOperation step, ProofReason reason, String state) {
        return ParseModalNextUntilAction.parseWithReason(proof, step, reason, state);
    }

    @Override
    protected AbstractModalAction parseAction(String name, List<Integer> sources, ModalOperation expression, String state) {
        return ParseModalNextUntilAction.parseAction(name, sources, expression, state);
    }
}
