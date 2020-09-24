package com.dan323.classical.proof;

import com.dan323.classical.ClassicAndE1;
import com.dan323.classical.ClassicAndE2;
import com.dan323.classical.ClassicAndI;
import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicFI;
import com.dan323.classical.ClassicModusPonens;
import com.dan323.classical.ClassicNotE;
import com.dan323.classical.ClassicNotI;
import com.dan323.classical.ClassicOrE;
import com.dan323.classical.ClassicOrI1;
import com.dan323.classical.ClassicOrI2;
import com.dan323.classical.ClassicalAction;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.generic.proof.ProofReason;

import java.util.Arrays;

public final class ParseClassicalAction {

    private ParseClassicalAction() {
    }

    public static ClassicalAction parse(NaturalDeduction naturalDeduction, int pos) {
        ProofReason proofReason = naturalDeduction.getSteps().get(pos - 1).getProof();
        return switch (proofReason.getNameProof()) {
            case "Ass" -> parseAss(naturalDeduction, pos);
            case "|I" -> parseOrI(naturalDeduction, pos, proofReason);
            case "|E" -> parseOrE(proofReason);
            case "&I" -> parseAndI(proofReason);
            case "&E" -> parseAndE(naturalDeduction, pos, proofReason);
            case "Rep" -> parseCopy(proofReason);
            case "-E" -> parseNotE(proofReason);
            case "-I" -> parseNotI();
            case "->I" -> parseImpI();
            case "->E" -> parseImpE(proofReason);
            case "FE" -> parseFE(naturalDeduction, pos, proofReason);
            case "FI" -> parseFI(proofReason);
            default -> throw new IllegalStateException();
        };
    }

    private static ClassicFI parseFI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ClassicFI(ints[0], ints[1]);
    }

    private static ClassicFE parseFE(NaturalDeduction naturalDeduction, int pos, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        ClassicalLogicOperation operation = naturalDeduction.getSteps().get(pos - 1).getStep();
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

    private static ClassicalAction parseOrI(NaturalDeduction naturalDeduction, int pos, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        ClassicalLogicOperation origin = naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        DisjunctionClassic disj = (DisjunctionClassic) naturalDeduction.getSteps().get(pos - 1).getStep();
        if (disj.getLeft().equals(origin)) {
            return new ClassicOrI1(ints[0], disj.getRight());
        } else {
            return new ClassicOrI2(ints[0], disj.getLeft());
        }
    }

    private static ClassicalAction parseAndE(NaturalDeduction naturalDeduction, int pos, ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        ConjunctionClassic conj = (ConjunctionClassic) naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        ClassicalLogicOperation conclusion = naturalDeduction.getSteps().get(pos - 1).getStep();
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
        return Arrays.stream(proofReason.toString().substring(proofReason.getNameProof().length() + 2, proofReason.toString().length() - 1).split(","))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    private static ClassicAssume parseAss(NaturalDeduction naturalDeduction, int pos) {
        return new ClassicAssume(naturalDeduction.getSteps().get(pos - 1).getStep());
    }

    private static ClassicOrE parseOrE(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ClassicOrE(ints[0], ints[1], ints[2]);
    }

}
