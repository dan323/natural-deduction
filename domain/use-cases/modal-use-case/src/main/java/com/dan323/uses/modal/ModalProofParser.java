package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
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
            state = line.substring(0, i);
            isRelation = false;
            line = line.substring(i + 2);
        }
        var array = line.toCharArray();
        i = 0;
        while (array[i] == ' ') {
            i++;
        }
        int assmsLevel = i / 3;
        var startExpression = line.substring(i);
        var firstSpace = startExpression.indexOf(" ".repeat(11));
        var lastSpace = startExpression.lastIndexOf("  ");
        var expression = startExpression.substring(0, firstSpace);
        var rule = startExpression.substring(lastSpace + 2);
        if (isRelation) {
            return new ProofStepModal(assmsLevel, (RelationOperation) ParseModalAction.parseExpression(expression), ParseModalAction.parseReason(rule));
        } else {
            return new ProofStepModal(state, assmsLevel, (ModalLogicalOperation) ParseModalAction.parseExpression(expression), ParseModalAction.parseReason(rule));
        }
    }

}
