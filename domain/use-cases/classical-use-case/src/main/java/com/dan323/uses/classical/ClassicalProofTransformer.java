package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ActionExpression;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassicalProofTransformer implements Transformer<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction, ClassicalAction> {

    private static final List<ActionDescriptorDto> ACTIONS = new ClassicGetActions().perform();

    @Override
    public String logic() {
        return "classical";
    }

    public NaturalDeduction from(ProofDto proof) {
        try {
            return replayProof(proof);
        } catch (InvalidProofException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidProofException("The proof could not be read, check its expressions and rules", e);
        }
    }

    private static NaturalDeduction replayProof(ProofDto proof) {
        NaturalDeduction nd = new NaturalDeduction();
        List<ClassicalLogicOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        if (proof.steps().isEmpty()) {
            nd.initializeProof(List.of(), ParseClassicalAction.parseExpression(proof.goal()));
        } else {
            int line = 0;
            for (StepDto step : proof.steps()) {
                line++;
                if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                    assmsLst.add(ParseClassicalAction.parseExpression(step.expression()));
                } else {
                    if (assms) {
                        assms = false;
                        nd.initializeProof(assmsLst, ParseClassicalAction.parseExpression(proof.goal()));
                    }
                    replay(nd, step, line);
                }
            }
            if (assms) {
                nd.initializeProof(assmsLst, ParseClassicalAction.parseExpression(proof.goal()));
            }
        }
        return nd;
    }

    private static void replay(NaturalDeduction nd, StepDto step, int line) {
        ClassicalAction action;
        try {
            action = ParseClassicalAction.parseWithReason(nd, ParseClassicalAction.parseExpression(step.expression()), ParseClassicalAction.parseReason(step.rule()));
        } catch (RuntimeException e) {
            throw new InvalidProofException("Line " + line + " is not valid: cannot read '" + step.expression() + "' justified by '" + step.rule() + "'", e);
        }
        if (!action.isValid(nd)) {
            throw new InvalidProofException("Line " + line + " does not follow: '" + step.rule() + "' cannot justify '" + step.expression() + "'");
        }
        action.apply(nd);
    }

    public ClassicalAction from(ActionDto action) {
        return ParseClassicalAction.parseAction(action.name(), action.sources(), ActionExpression.of(action, ACTIONS)
                .map(ParseClassicalAction::parseExpression).orElse(null));
    }

    @Override
    public ProofDto fromProof(NaturalDeduction proof) {
        var logic = "classical";
        var goal = proof.getGoal().toString();
        var steps = new ArrayList<StepDto>();
        for (var step : proof.getSteps()) {
            steps.add(new StepDto(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel(), Map.of()));
        }
        return new ProofDto(steps.stream().toList(), logic, goal);
    }

}
