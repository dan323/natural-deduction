package com.dan323.classical.proof;

import com.dan323.classical.*;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ClassicalParser;
import com.dan323.expressions.classical.ConjunctionClassic;
import com.dan323.expressions.classical.DisjunctionClassic;
import com.dan323.proof.generic.proof.ProofReason;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class ParseClassicalAction {

    private static final ClassicalParser classicalParser = new ClassicalParser();

    private ParseClassicalAction() {
    }

    public static ClassicalAction parseWithReason(NaturalDeduction naturalDeduction, ClassicalLogicOperation atPos, ProofReason proofReason) {
        return switch (proofReason.getNameProof()) {
            case "Ass" -> parseAss(atPos);
            case "|I" -> parseOrI(naturalDeduction, atPos, proofReason);
            case "|E" -> parseOrE(proofReason);
            case "&I" -> parseAndI(proofReason);
            case "&E" -> parseAndE(naturalDeduction, atPos, proofReason);
            case "Rep" -> parseCopy(proofReason);
            case "-E" -> parseNotE(proofReason);
            case "-I" -> parseNotI();
            case "->I" -> parseImpI();
            case "->E" -> parseImpE(proofReason);
            case "FE" -> parseFE(atPos, proofReason);
            case "FI" -> parseFI(proofReason);
            default -> throw new IllegalStateException();
        };
    }

    public static ClassicalAction parse(NaturalDeduction naturalDeduction, int pos) {
        ProofReason proofReason = naturalDeduction.getSteps().get(pos - 1).getProof();
        ClassicalLogicOperation atPos = naturalDeduction.getSteps().get(pos - 1).getStep();
        return parseWithReason(naturalDeduction, atPos, proofReason);
    }

    private static ClassicFI parseFI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicFI(ints[0], ints[1]);
    }

    private static ClassicFE parseFE(ClassicalLogicOperation operation, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicFE(ints[0], operation);
    }

    private static ClassicModusPonens parseImpE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicModusPonens(ints[0], ints[1]);
    }

    private static ClassicDeductionTheorem parseImpI() {
        return new ClassicDeductionTheorem();
    }

    private static ClassicNotI parseNotI() {
        return new ClassicNotI();
    }

    private static ClassicalAction parseOrI(NaturalDeduction naturalDeduction, ClassicalLogicOperation atPos, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        ClassicalLogicOperation origin = naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (atPos instanceof DisjunctionClassic disj) {
            if (disj.getLeft().equals(origin)) {
                return new ClassicOrI1(ints[0], disj.getRight());
            } else {
                return new ClassicOrI2(ints[0], disj.getLeft());
            }
        } else {
            throw new IllegalArgumentException(atPos.toString() + " is not Disjunction");
        }
    }

    private static ClassicalAction parseAndE(NaturalDeduction naturalDeduction, ClassicalLogicOperation conclusion, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        ConjunctionClassic conj = (ConjunctionClassic) naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (conclusion.equals(conj.getLeft())) {
            return new ClassicAndE1(ints[0]);
        } else {
            return new ClassicAndE2(ints[0]);
        }
    }

    private static ClassicNotE parseNotE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicNotE(ints[0]);
    }

    private static ClassicCopy parseCopy(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicCopy(ints[0]);
    }

    private static ClassicAndI parseAndI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicAndI(ints[0], ints[1]);
    }

    private static int[] parseArray(ProofReason proofReason) {
        return Arrays.stream(proofReason.toString().substring(proofReason.getNameProof().length() + 2, proofReason.toString().length() - 1)
                .split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static ClassicAssume parseAss(ClassicalLogicOperation atPos) {
        return new ClassicAssume(atPos);
    }

    private static ClassicOrE parseOrE(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ClassicOrE(ints[0], ints[1], ints[2]);
    }

    public static ClassicalLogicOperation parseExpression(String expression) {
        return classicalParser.evaluate(expression);
    }

    public static ProofReason parseReason(String ruleString) {
        return ProofReason.parseReason(ruleString, new HashMap<>());
    }

    public static ClassicalAction parseAction(String name, List<Integer> sources, ClassicalLogicOperation extraInfo){
        return switch (name) {
            case "ClassicAssume" -> new ClassicAssume(extraInfo);
            case "ClassicOrI1" -> new ClassicOrI1(sources.get(0), extraInfo);
            case "ClassicOrI2" -> new ClassicOrI2(sources.get(0), extraInfo);
            case "ClassicOrE" -> new ClassicOrE(sources.get(0), sources.get(1), sources.get(2));
            case "ClassicAndI" -> new ClassicAndI(sources.get(0), sources.get(1));
            case "ClassicAndE1" -> new ClassicAndE1(sources.get(0));
            case "ClassicAndE2" -> new ClassicAndE2(sources.get(0));
            case "ClassicCopy" -> new ClassicCopy(sources.getFirst());
            case "ClassicNotE" -> new ClassicNotE(sources.getFirst());
            case "ClassicNotI" -> new ClassicNotI();
            case "ClassicDeductionTheorem" -> new ClassicDeductionTheorem();
            case "ClassicModusPonens" -> new ClassicModusPonens(sources.get(0), sources.get(1));
            case "ClassicFE" -> new ClassicFE(sources.get(0), extraInfo);
            case "ClassicFI" -> new ClassicFI(sources.get(0), sources.get(1));
            default -> throw new IllegalArgumentException("The rule " + name + " is not valid.");
        };
    }
}
