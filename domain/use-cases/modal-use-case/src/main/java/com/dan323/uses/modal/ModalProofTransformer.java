package com.dan323.uses.modal;

import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModalProofTransformer implements Transformer<ModalOperation, ProofStepModal, ModalNaturalDeduction, AbstractModalAction> {


    @Override
    public String logic() {
        return "modal";
    }

    @Override
    public ModalNaturalDeduction from(ProofDto proof) {
        ModalNaturalDeduction nd = new ModalNaturalDeduction("s0");
        List<ModalOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        for (StepDto step : proof.steps()) {
            if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                var operation = ParseModalAction.parseExpression(step.expression());
                if (operation instanceof ModalLogicalOperation) {
                    if (step.extraParameters().containsKey("state") && step.extraParameters().get("state").equals(nd.getState0())) {
                        assmsLst.add(operation);
                    } else {
                        throw new IllegalArgumentException("The assumptions are not in a valid state");
                    }
                } else {
                    assmsLst.add(ParseModalAction.parseExpression(step.expression()));
                }
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, ParseModalAction.parseExpression(proof.goal()));
                }
                ParseModalAction.parseWithReason(nd, ParseModalAction.parseExpression(step.expression()), ParseModalAction.parseReason(step.rule()), step.extraParameters().get("state")).apply(nd);
            }
        }
        if (assms) {
            nd.initializeProof(assmsLst, ParseModalAction.parseExpression(proof.goal()));
        }
        return nd;
    }

    public AbstractModalAction from(ActionDto action) {
        var parser = new ModalLogicParser();
        var expression = action.extraParameters().get("expression");
        return ParseModalAction.parseAction(action.name(), action.sources(), expression == null ? null : parser.evaluate(expression), action.extraParameters().get("state"));
    }

    @Override
    public ProofDto fromProof(ModalNaturalDeduction proof) {
        var logic = "modal";
        var goal = proof.getGoal().toString();
        var steps = new ArrayList<StepDto>();
        for (var step : proof.getSteps()) {
            steps.add(new StepDto(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel(), step.getState() != null ? Map.of("state", step.getState()) : Map.of()));
        }
        return new ProofDto(steps.stream().toList(), logic, goal);

    }
}
