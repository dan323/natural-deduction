package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.ProofParser;

/**
 * Parses a modal proof text file. A logic that extends modal logic overrides the protected methods that read its
 * formulas and rules.
 */
public class ModalProofParser implements ProofParser<ModalNaturalDeduction, ModalOperation, ProofStepModal, ModalAction> {

    public String logic() {
        return "modal";
    }

    @Override
    public ModalNaturalDeduction getNewProof() {
        return new ModalNaturalDeduction();
    }

    protected ModalOperation parseExpression(String expression) {
        return ParseModalAction.parseExpression(expression);
    }

    protected ProofReason parseReason(String rule) {
        return ParseModalAction.parseReason(rule);
    }


    public ProofStepModal parseLine(String line) {
        int i = line.indexOf(':');
        boolean isRelation = true;
        String state = null;
        if (i != -1) {
            if (i + 2 > line.length()) {
                throw new InvalidProofException("expected an expression after the state");
            }
            state = line.substring(0, i);
            isRelation = false;
            line = line.substring(i + 2);
        }
        var parts = ProofLine.split(line);
        var reason = parseReason(parts.rule());
        if (reason == null) {
            throw new InvalidProofException("unknown rule '" + parts.rule() + "'");
        }
        var expression = parseExpression(parts.expression());
        if (isRelation) {
            return new ProofStepModal(parts.assmsLevel(), (RelationOperation) expression, reason);
        } else {
            return new ProofStepModal(state, parts.assmsLevel(), (ModalLogicalOperation) expression, reason);
        }
    }

}
