package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ClassicalParser;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClassicalProofTransformer implements Transformer<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction, ClassicalAction> {

    @Override
    public String logic() {
        return "classical";
    }

    public NaturalDeduction from(ProofDto proof) {
        NaturalDeduction nd = new NaturalDeduction();
        List<ClassicalLogicOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        if (proof.steps().isEmpty()) {
            nd.initializeProof(List.of(), ParseClassicalAction.parseExpression(proof.goal()));
        } else {
            for (StepDto step : proof.steps()) {
                if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                    assmsLst.add(ParseClassicalAction.parseExpression(step.expression()));
                } else {
                    if (assms) {
                        assms = false;
                        nd.initializeProof(assmsLst, ParseClassicalAction.parseExpression(proof.goal()));
                    }
                    ParseClassicalAction.parseWithReason(nd, ParseClassicalAction.parseExpression(step.expression()), ParseClassicalAction.parseReason(step.rule())).apply(nd);
                }
            }
            if (assms) {
                nd.initializeProof(assmsLst, ParseClassicalAction.parseExpression(proof.goal()));
            }
        }
        return nd;
    }

    public ClassicalAction from(ActionDto action) {
        var parser = new ClassicalParser();
        return ParseClassicalAction.parseAction(action.name(), action.sources(), Optional.ofNullable(action.extraParameters().get("expression")).map(parser::evaluate).orElse(null));
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
