package com.dan323.proof.modal.proof;

import com.dan323.expresions.modal.ConjunctionModal;
import com.dan323.expresions.modal.DisjunctionModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;

import java.util.Arrays;

public final class ParseModalAction {

    private ParseModalAction() {
    }

    public static <T> AbstractModalAction<T> parse(ModalNaturalDeduction<T> proof, int pos) {
        return switch (proof.getSteps().get(pos - 1).getProof().getNameProof()) {
            case "Ass" -> parseAss(proof, pos);
            case "|I" -> parseOrI(proof, pos);
            case "|E" -> parseOrE(proof.getSteps().get(pos - 1).getProof());
            case "&I" -> parseAndI(proof.getSteps().get(pos - 1).getProof());
            case "&E" -> parseAndE(proof, pos);
            case "Rep" -> parseCopy(proof.getSteps().get(pos - 1).getProof());
            case "-E" -> parseNotE(proof.getSteps().get(pos - 1).getProof());
            case "-I" -> parseNotI();
            case "->I" -> parseImpI();
            case "->E" -> parseImpE(proof.getSteps().get(pos - 1).getProof());
            case "FE" -> parseFE(proof, pos);
            case "FI" -> parseFI(proof, pos);
            case "[]I" -> parseBoxI();
            case "[]E" -> parseBoxE(proof.getSteps().get(pos - 1).getProof());
            case "<>I" -> parseDiaI(proof.getSteps().get(pos - 1).getProof());
            case "<>E" -> parseDiaE(proof.getSteps().get(pos - 1).getProof());
            case "Refl" -> parseRefl(proof.getSteps().get(pos - 1).getProof());
            case "Trans" -> parseTrans(proof.getSteps().get(pos - 1).getProof());
            default -> throw new IllegalStateException();
        };
    }

    private static <T> AbstractModalAction<T> parseTrans(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new Transitive<>(ints[0], ints[1]);
    }

    private static <T> AbstractModalAction<T> parseRefl(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new Reflexive<>(ints[0]);
    }

    private static <T> ModalAction<T> parseDiaE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalDiaE<>(ints[0]);
    }

    private static <T> ModalDiaI<T> parseDiaI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalDiaI<>(ints[0], ints[1]);
    }

    private static <T> ModalBoxE<T> parseBoxE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalBoxE<>(ints[0], ints[1]);
    }

    private static <T> ModalBoxI<T> parseBoxI() {
        return new ModalBoxI<>();
    }

    private static <T> ModalFI<T> parseFI(ModalNaturalDeduction<T> naturalDeduction, int pos) {
        int[] ints = parseArray(naturalDeduction.getSteps().get(pos - 1).getProof());
        return new ModalFI<>(naturalDeduction.getSteps().get(pos - 1).getState(), ints[0], ints[1]);
    }

    private static <T> ModalFE<T> parseFE(ModalNaturalDeduction<T> naturalDeduction, int pos) {
        int[] ints = parseArray(naturalDeduction.getSteps().get(pos - 1).getProof());
        ModalOperation operation = naturalDeduction.getSteps().get(pos - 1).getStep();
        return new ModalFE<>(ints[0], (ModalLogicalOperation) operation, naturalDeduction.getSteps().get(pos - 1).getState());
    }

    private static <T> ModalModusPonens<T> parseImpE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalModusPonens<>(ints[0], ints[1]);
    }

    private static <T> ModalDeductionTheorem<T> parseImpI() {
        return new ModalDeductionTheorem<>();
    }

    private static <T> ModalNotI<T> parseNotI() {
        return new ModalNotI<>();
    }

    private static <T> ModalAction<T> parseOrI(ModalNaturalDeduction<T> naturalDeduction, int pos) {
        int[] ints = parseArray(naturalDeduction.getSteps().get(pos - 1).getProof());
        ModalOperation origin = naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        DisjunctionModal disj = (DisjunctionModal) naturalDeduction.getSteps().get(pos - 1).getStep();
        if (disj.getLeft().equals(origin)) {
            return new ModalOrI1<>(ints[0], (ModalLogicalOperation) disj.getRight());
        } else {
            return new ModalOrI2<>(ints[0], (ModalLogicalOperation) disj.getLeft());
        }
    }

    private static <T> ModalAction<T> parseAndE(ModalNaturalDeduction<T> naturalDeduction, int pos) {
        int[] ints = parseArray(naturalDeduction.getSteps().get(pos - 1).getProof());
        ConjunctionModal conj = (ConjunctionModal) naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        ModalOperation conclusion = naturalDeduction.getSteps().get(pos - 1).getStep();
        if (conclusion.equals(conj.getLeft())) {
            return new ModalAndE1<>(ints[0]);
        } else {
            return new ModalAndE2<>(ints[0]);
        }
    }

    private static <T> ModalNotE<T> parseNotE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalNotE<>(ints[0]);
    }

    private static <T> ModalCopy<T> parseCopy(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalCopy<>(ints[0]);
    }

    private static <T> ModalAndI<T> parseAndI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalAndI<>(ints[0], ints[1]);
    }

    private static int[] parseArray(ProofReason proofReason) {
        return Arrays.stream(proofReason.toString().substring(proofReason.getNameProof().length() + 2, proofReason.toString().length() - 1).split(","))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    private static <T> ModalAssume<T> parseAss(ModalNaturalDeduction<T> naturalDeduction, int pos) {
        ProofStepModal<T> step = naturalDeduction.getSteps().get(pos - 1);
        if (step.getStep() instanceof RelationOperation) {
            return new ModalAssume<>((RelationOperation<T>) step.getStep());
        } else {
            return new ModalAssume<>((ModalLogicalOperation) step.getStep(), step.getState());
        }
    }

    private static <Q> ModalOrE<Q> parseOrE(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ModalOrE<>(ints[0], ints[1], ints[2]);
    }

}
