package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ProofParser;

public class ParseClassicalProof implements ProofParser<NaturalDeduction, ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, ClassicalAction> {

    public String logic() {
        return "classical";
    }

    @Override
    public NaturalDeduction getNewProof() {
        return new NaturalDeduction();
    }

    public ProofStep<ClassicalLogicOperation> parseLine(String line) {
        var array = line.toCharArray();
        int i = 0;
        while (array[i] == ' ') {
            i++;
        }
        int assmsLevel = i / 3;
        var startExpression = line.substring(i);
        var firstSpace = startExpression.indexOf(" ".repeat(11));
        var lastSpace = startExpression.lastIndexOf("  ");
        var expression = startExpression.substring(0, firstSpace);
        var rule = startExpression.substring(lastSpace + 2);
        return new ProofStep<>(assmsLevel, ParseClassicalAction.parseExpression(expression), ParseClassicalAction.parseReason(rule));
    }

}
