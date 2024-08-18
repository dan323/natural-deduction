package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.ProofParser;

public class ModalProofParser implements ProofParser<ModalNaturalDeduction, ModalOperation, ProofStepModal> {

    public String logic() {
        return "modal";
    }

    public ModalNaturalDeduction parseProof(String proof) {
        ModalNaturalDeduction nd = new ModalNaturalDeduction();
        var actions = proof.lines().map(ModalProofParser::parseLine).toList();
        var assms = actions.stream()
                .takeWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .map(ProofStep::getStep)
                .toList();
        var actualProof = actions.stream()
                .dropWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .toList();
        if (actions.getLast().getAssumptionLevel() == 0) {
            nd.initializeProof(assms, actions.getLast().getStep());
            nd.getSteps().addAll(actualProof);
            var parsedActions = nd.parse();
            nd.reset();
            int i = 0;
            for (AbstractModalAction action : parsedActions) {
                if (i < nd.getAssms().size()) {
                    i++;
                } else if (action.isValid(nd)) {
                    action.apply(nd);
                } else {
                    throw new IllegalArgumentException("The proof is invalid!!");
                }
            }
            return nd;
        } else {
            throw new IllegalArgumentException("The proof is invalid!!");
        }
    }

    private static ProofStepModal parseLine(String line) {
        int i = line.indexOf(':');
        if (i == -1) {
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
            return new ProofStepModal(assmsLevel, (RelationOperation) ParseModalAction.parseExpression(expression), ParseModalAction.parseReason(rule));
        } else {
            var array = line.substring(i + 2).toCharArray();
            String state = line.substring(0, i);
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
            return new ProofStepModal(state, assmsLevel, (ModalLogicalOperation) ParseModalAction.parseExpression(expression), ParseModalAction.parseReason(rule));
        }
    }

}
