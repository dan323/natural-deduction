package com.dan323.uses.modal;

import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.ActionExpression;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModalProofTransformer implements Transformer<ModalOperation, ProofStepModal, ModalNaturalDeduction, AbstractModalAction> {

    private static final List<ActionDescriptorDto> ACTIONS = new ModalGetActions().perform();

    @Override
    public String logic() {
        return "modal";
    }

    @Override
    public ModalNaturalDeduction from(ProofDto proof) {
        try {
            return replayProof(proof);
        } catch (InvalidProofException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidProofException("The proof could not be read, check its expressions and rules", e);
        }
    }

    private static ModalNaturalDeduction replayProof(ProofDto proof) {
        ModalNaturalDeduction nd = new ModalNaturalDeduction("s0");
        List<ModalOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        int line = 0;
        for (StepDto step : proof.steps()) {
            line++;
            if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                assmsLst.add(initialAssumption(nd, step, line));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, ParseModalAction.parseExpression(proof.goal()));
                }
                replay(nd, step, line);
            }
        }
        if (assms) {
            nd.initializeProof(assmsLst, ParseModalAction.parseExpression(proof.goal()));
        }
        return nd;
    }

    private static ModalOperation initialAssumption(ModalNaturalDeduction nd, StepDto step, int line) {
        var operation = ParseModalAction.parseExpression(step.expression());
        if (operation instanceof ModalLogicalOperation && !nd.getState0().equals(step.extraParameters().get("state"))) {
            throw new InvalidProofException("Line " + line + " is not valid: the assumptions are not in a valid state");
        }
        return operation;
    }

    private static void replay(ModalNaturalDeduction nd, StepDto step, int line) {
        AbstractModalAction action;
        try {
            action = ParseModalAction.parseWithReason(nd, ParseModalAction.parseExpression(step.expression()), ParseModalAction.parseReason(step.rule()), step.extraParameters().get("state"));
        } catch (RuntimeException e) {
            throw new InvalidProofException("Line " + line + " is not valid: cannot read '" + step.expression() + "' justified by '" + step.rule() + "'", e);
        }
        if (!action.isValid(nd)) {
            throw new InvalidProofException("Line " + line + " does not follow: '" + step.rule() + "' cannot justify '" + step.expression() + "'");
        }
        action.apply(nd);
    }

    public AbstractModalAction from(ActionDto action) {
        var parser = new ModalLogicParser();
        var expression = ActionExpression.of(action, ACTIONS);
        return ParseModalAction.parseAction(action.name(), action.sources(), expression.map(parser::evaluate).orElse(null), action.extraParameters().get("state"));
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
