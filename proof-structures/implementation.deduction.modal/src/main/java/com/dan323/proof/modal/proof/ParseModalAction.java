package com.dan323.proof.modal.proof;

import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ParseModalAction {

    private ParseModalAction() {
    }

    public static <T> AbstractModalAction<T> parseWithReason(ModalNaturalDeduction<T> proof, ModalOperation atPos, ProofReason proofReason, T state) {
        return switch (proofReason.getNameProof()) {
            case "Ass" -> parseAss(atPos, state);
            case "|I" -> parseOrI(proof, proofReason, atPos);
            case "|E" -> parseOrE(proofReason);
            case "&I" -> parseAndI(proofReason);
            case "&E" -> parseAndE(proof, proofReason, atPos);
            case "Rep" -> parseCopy(proofReason);
            case "-E" -> parseNotE(proofReason);
            case "-I" -> parseNotI();
            case "->I" -> parseImpI();
            case "->E" -> parseImpE(proofReason);
            case "FE" -> parseFE(proofReason, atPos, state);
            case "FI" -> parseFI(proofReason, state);
            case "[]I" -> parseBoxI();
            case "[]E" -> parseBoxE(proofReason);
            case "<>I" -> parseDiaI(proofReason);
            case "<>E" -> parseDiaE(proofReason);
            case "Refl" -> parseRefl(proofReason);
            case "Trans" -> parseTrans(proofReason);
            default -> throw new IllegalStateException();
        };
    }

    public static <T> AbstractModalAction<T> parse(ModalNaturalDeduction<T> proof, int pos) {
        return parseWithReason(proof, proof.getSteps().get(pos - 1).getStep(), proof.getSteps().get(pos - 1).getProof(), proof.getSteps().get(pos - 1).getState());
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

    private static <T> ModalFI<T> parseFI(ProofReason reason, T state) {
        int[] ints = parseArray(reason);
        return new ModalFI<>(state, ints[0], ints[1]);
    }

    private static <T> ModalFE<T> parseFE(ProofReason reason, ModalOperation atPos, T state) {
        int[] ints = parseArray(reason);
        return new ModalFE<>(ints[0], (ModalLogicalOperation) atPos, state);
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

    private static <T> ModalAction<T> parseOrI(ModalNaturalDeduction<T> naturalDeduction, ProofReason reason, ModalOperation atPos) {
        int[] ints = parseArray(reason);
        ModalOperation origin = naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (((DisjunctionModal) atPos).getLeft().equals(origin)) {
            return new ModalOrI1<>(ints[0], (ModalLogicalOperation) ((DisjunctionModal) atPos).getRight());
        } else {
            return new ModalOrI2<>(ints[0], (ModalLogicalOperation) ((DisjunctionModal) atPos).getLeft());
        }
    }

    private static <T> ModalAction<T> parseAndE(ModalNaturalDeduction<T> naturalDeduction, ProofReason reason, ModalOperation atPos) {
        int[] ints = parseArray(reason);
        ConjunctionModal conj = (ConjunctionModal) naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (atPos.equals(conj.getLeft())) {
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

    private static <T> ModalAssume<T> parseAss(ModalOperation atPos, T state) {
        if (atPos instanceof RelationOperation) {
            return new ModalAssume<>((RelationOperation<T>) atPos);
        } else {
            return new ModalAssume<>((ModalLogicalOperation) atPos, state);
        }
    }

    private static <Q> ModalOrE<Q> parseOrE(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ModalOrE<>(ints[0], ints[1], ints[2]);
    }

    private static int[] parseArray(String proofReason, int reasonLength) {
        return Arrays.stream(proofReason.substring(reasonLength + 2, proofReason.length() - 1)
                .split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static final Map<String, Function<String, ProofReason>> MODAL_RULES =
            Map.of("[]I", st -> new ProofReason("[]I", Arrays.stream(parseArray(st, 3)).boxed().collect(Collectors.toUnmodifiableList())),
                    "[]E", st -> new ProofReason("[]E", Arrays.stream(parseArray(st, 3)).boxed().collect(Collectors.toUnmodifiableList())),
                    "<>I", st -> new ProofReason("<>I", Arrays.stream(parseArray(st, 3)).boxed().collect(Collectors.toUnmodifiableList())),
                    "<>E", st -> new ProofReason("<>E", Arrays.stream(parseArray(st, 3)).boxed().collect(Collectors.toUnmodifiableList())),
                    "Ref", st -> new ProofReason("Refl", Arrays.stream(parseArray(st, 4)).boxed().collect(Collectors.toUnmodifiableList())),
                    "Tra", st -> new ProofReason("Trans", Arrays.stream(parseArray(st, 5)).boxed().collect(Collectors.toUnmodifiableList())));


    public static ProofReason parseReason(String ruleString) {
        return ProofReason.parseReason(ruleString, MODAL_RULES);
    }

    private static final ModalLogicParser<String> modalParser = new ModalLogicParser<>(Function.identity());

    public static ModalOperation parseExpression(String expression) {
        return modalParser.evaluate(expression);
    }
}
