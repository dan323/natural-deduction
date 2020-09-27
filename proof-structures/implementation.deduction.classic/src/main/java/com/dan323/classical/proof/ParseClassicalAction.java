package com.dan323.classical.proof;

import com.dan323.classical.*;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ClassicalParser;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.generic.proof.ProofReason;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ParseClassicalAction {

    private static final ClassicalParser classicalParser = new ClassicalParser();

    private ParseClassicalAction() {
    }

    public static ClassicalAction parseWithReason(NaturalDeduction naturalDeduction, ClassicalLogicOperation atPos, ProofReason proofReason) {
        return switch (proofReason.getNameProof()) {
            case "Ass" -> parseAss(naturalDeduction, atPos);
            case "|I" -> parseOrI(naturalDeduction, atPos, proofReason);
            case "|E" -> parseOrE(proofReason);
            case "&I" -> parseAndI(proofReason);
            case "&E" -> parseAndE(naturalDeduction, atPos, proofReason);
            case "Rep" -> parseCopy(proofReason);
            case "-E" -> parseNotE(proofReason);
            case "-I" -> parseNotI();
            case "->I" -> parseImpI();
            case "->E" -> parseImpE(proofReason);
            case "FE" -> parseFE(naturalDeduction, atPos, proofReason);
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

    private static ClassicFE parseFE(NaturalDeduction naturalDeduction, ClassicalLogicOperation operation, ProofReason proofReason) {
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
        DisjunctionClassic disj = (DisjunctionClassic) atPos;
        if (disj.getLeft().equals(origin)) {
            return new ClassicOrI1(ints[0], disj.getRight());
        } else {
            return new ClassicOrI2(ints[0], disj.getLeft());
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

    private static int[] parseArray(String proofReason, int reasonLength) {
        return Arrays.stream(proofReason.substring(reasonLength + 2, proofReason.length() - 1)
                .split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static ClassicAssume parseAss(NaturalDeduction naturalDeduction, ClassicalLogicOperation atPos) {
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
        return switch (ruleString.substring(0, 3)) {
            case "Ass" -> new ProofReason("Ass", List.of());
            case "|I " -> new ProofReason("|I", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "|E " -> new ProofReason("|E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "&I " -> new ProofReason("&I", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "&E " -> new ProofReason("&E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "Rep" -> new ProofReason("Rep", Arrays.stream(parseArray(ruleString, 3)).boxed().collect(Collectors.toUnmodifiableList()));
            case "-E " -> new ProofReason("-E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "-I " -> new ProofReason("-I", List.of());
            case "->I" -> new ProofReason("->I", List.of());
            case "->E" -> new ProofReason("->E", Arrays.stream(parseArray(ruleString, 3)).boxed().collect(Collectors.toUnmodifiableList()));
            case "FE " -> new ProofReason("FE", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "FI " -> new ProofReason("FI", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            default -> throw new IllegalArgumentException();
        };
    }

    public static ClassicalAction parseAction(String name, List<Integer> sources, ClassicalLogicOperation extraInfo){
        return switch (name) {
            case "Ass" -> new ClassicAssume(extraInfo);
            case "|I1" -> new ClassicOrI1(sources.get(0), extraInfo);
            case "|I2" -> new ClassicOrI2(sources.get(0), extraInfo);
            case "|E" -> new ClassicOrE(sources.get(0), sources.get(1), sources.get(2));
            case "&I" -> new ClassicAndI(sources.get(0), sources.get(1));
            case "&E1" -> new ClassicAndE1(sources.get(0));
            case "&E2" -> new ClassicAndE2(sources.get(0));
            case "Rep" -> new ClassicCopy(sources.get(0));
            case "-E" -> new ClassicNotE(sources.get(0));
            case "-I" -> new ClassicNotI();
            case "->I" -> new ClassicDeductionTheorem();
            case "->E" -> new ClassicModusPonens(sources.get(0), sources.get(1));
            case "FE" -> new ClassicFE(sources.get(0), extraInfo);
            case "FI" -> new ClassicFI(sources.get(0), sources.get(1));
            default -> throw new IllegalArgumentException();
        };
    }
}
