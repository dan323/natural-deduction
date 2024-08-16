package com.dan323.classical.proof;

import com.dan323.classical.ClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.ProofParser;
import com.dan323.proof.generic.proof.ProofStep;

public class ParseClassicalProof implements ProofParser<NaturalDeduction,ClassicalLogicOperation,ProofStep<ClassicalLogicOperation>> {

    public String logic(){
        return "classical";
    }

    public NaturalDeduction parseProof(String proof) {
        NaturalDeduction nd = new NaturalDeduction();
        var actions = proof.lines().map(ParseClassicalProof::parseLine).toList();
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
            for (ClassicalAction action : parsedActions) {
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

    private static ProofStep<ClassicalLogicOperation> parseLine(String line) {
        var array = line.toCharArray();
        int i = 0;
        while (array[i] == ' ') {
            i++;
        }
        int assmsLevel = i / 3;
        var startExpression = line.substring(i);
        var firstSpace = startExpression.indexOf(' ');
        var lastSpace = startExpression.lastIndexOf("  ");
        var expression = startExpression.substring(0, firstSpace);
        var rule = startExpression.substring(lastSpace + 2);
        return new ProofStep<>(assmsLevel, ParseClassicalAction.parseExpression(expression), ParseClassicalAction.parseReason(rule));
    }

}
