package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.ProofParser;

public class ModalProofParser implements ProofParser<ModalNaturalDeduction, ModalOperation, ProofStepModal, ModalAction> {

    public String logic() {
        return "modal";
    }

    @Override
    public ModalNaturalDeduction getNewProof() {
        return new ModalNaturalDeduction();
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
        var reason = ParseModalAction.parseReason(parts.rule());
        if (reason == null) {
            throw new InvalidProofException("unknown rule '" + parts.rule() + "'");
        }
        var expression = ParseModalAction.parseExpression(parts.expression());
        if (isRelation) {
            return new ProofStepModal(parts.assmsLevel(), (RelationOperation) expression, reason);
        } else {
            return new ProofStepModal(state, parts.assmsLevel(), (ModalLogicalOperation) expression, reason);
        }
    }

}
