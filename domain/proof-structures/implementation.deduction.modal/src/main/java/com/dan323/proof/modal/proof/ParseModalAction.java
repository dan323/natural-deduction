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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ParseModalAction {

    private static final String TRANS = "Trans";
    private static final String REFL = "Refl";

    private ParseModalAction() {
    }

    public static AbstractModalAction parseWithReason(ModalNaturalDeduction proof, ModalOperation atPos, ProofReason proofReason, String state) {
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
            case "FI" -> parseFI(proofReason);
            case "[]I" -> parseBoxI();
            case "[]E" -> parseBoxE(proofReason);
            case "<>I" -> parseDiaI(proofReason);
            case "<>E" -> parseDiaE(proofReason);
            case REFL -> parseRefl(proofReason);
            case TRANS -> parseTrans(proofReason);
            default -> throw new IllegalStateException();
        };
    }

    public static AbstractModalAction parse(ModalNaturalDeduction proof, int pos) {
        return parseWithReason(proof, proof.getSteps().get(pos - 1).getStep(), proof.getSteps().get(pos - 1).getProof(), proof.getSteps().get(pos - 1).getState());
    }

    private static AbstractModalAction parseTrans(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new Transitive(ints[0], ints[1]);
    }

    private static AbstractModalAction parseRefl(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new Reflexive(ints[0]);
    }

    private static ModalAction parseDiaE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalDiaE(ints[0]);
    }

    private static ModalDiaI parseDiaI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalDiaI(ints[0], ints[1]);
    }

    private static ModalBoxE parseBoxE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalBoxE(ints[0], ints[1]);
    }

    private static ModalBoxI parseBoxI() {
        return new ModalBoxI();
    }

    private static ModalFI parseFI(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ModalFI(ints[0], ints[1]);
    }

    private static ModalFE parseFE(ProofReason reason, ModalOperation atPos, String state) {
        int[] ints = parseArray(reason);
        return new ModalFE(ints[0], (ModalLogicalOperation) atPos, state);
    }

    private static ModalModusPonens parseImpE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalModusPonens(ints[0], ints[1]);
    }

    private static ModalDeductionTheorem parseImpI() {
        return new ModalDeductionTheorem();
    }

    private static ModalNotI parseNotI() {
        return new ModalNotI();
    }

    private static ModalAction parseOrI(ModalNaturalDeduction naturalDeduction, ProofReason reason, ModalOperation atPos) {
        int[] ints = parseArray(reason);
        ModalOperation origin = naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (((DisjunctionModal) atPos).getLeft().equals(origin)) {
            return new ModalOrI1(ints[0], ((DisjunctionModal) atPos).getRight());
        } else {
            return new ModalOrI2(ints[0], ((DisjunctionModal) atPos).getLeft());
        }
    }

    private static ModalAction parseAndE(ModalNaturalDeduction naturalDeduction, ProofReason reason, ModalOperation atPos) {
        int[] ints = parseArray(reason);
        ConjunctionModal conj = (ConjunctionModal) naturalDeduction.getSteps().get(ints[0] - 1).getStep();
        if (atPos.equals(conj.getLeft())) {
            return new ModalAndE1(ints[0]);
        } else {
            return new ModalAndE2(ints[0]);
        }
    }

    private static ModalNotE parseNotE(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalNotE(ints[0]);
    }

    private static ModalCopy parseCopy(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalCopy(ints[0]);
    }

    private static ModalAndI parseAndI(ProofReason proofReason) {
        int[] ints = parseArray(proofReason);
        return new ModalAndI(ints[0], ints[1]);
    }

    private static int[] parseArray(ProofReason proofReason) {
        return Arrays.stream(proofReason.toString().substring(proofReason.getNameProof().length() + 2, proofReason.toString().length() - 1)
                        .split(","))
                .map(String::trim)
                .filter(split -> !split.contains("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static ModalAssume parseAss(ModalOperation atPos, String state) {
        if (atPos instanceof RelationOperation operation) {
            return new ModalAssume(operation);
        } else if (state != null) {
            return new ModalAssume((ModalLogicalOperation) atPos, state);
        } else {
            throw new IllegalArgumentException("State provided is invalid.");
        }
    }

    private static ModalOrE parseOrE(ProofReason reason) {
        int[] ints = parseArray(reason);
        return new ModalOrE(ints[0], ints[1], ints[2]);
    }

    private static int[] parseArray(String proofReason, int reasonLength) {
        return Arrays.stream(proofReason.substring(reasonLength + 2, proofReason.length() - 1)
                        .split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static int[] parseRange(String proofReason) {
        return Arrays.stream(proofReason.split("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static final Map<String, Function<String, ProofReason>> MODAL_RULES =
            Map.of("[]I", st -> {
                        var lst = parseRange(st.substring(5, st.length()-1));
                        return new ProofReason("[]I", List.of(new ProofReason.Range(lst[0], lst[1])), List.of());
                    },
                    "[]E", st -> new ProofReason("[]E", List.of(), Arrays.stream(parseArray(st, 3)).boxed().toList()),
                    "<>I", st -> new ProofReason("<>I", List.of(), Arrays.stream(parseArray(st, 3)).boxed().toList()),
                    "<>E", st -> {
                        var lst = Arrays.stream(st.substring(5, st.length() -1).split(",")).map(String::trim).toArray(String[]::new);
                        var range = parseRange(lst[1]);
                        return new ProofReason("<>E", List.of(new ProofReason.Range(range[0], range[1])), List.of(Integer.parseInt(lst[0])));
                    },
                    "Ref", st -> new ProofReason(REFL, List.of(), Arrays.stream(parseArray(st, 4)).boxed().toList()),
                    "Tra", st -> new ProofReason(TRANS, List.of(), Arrays.stream(parseArray(st, 5)).boxed().toList()));


    public static ProofReason parseReason(String ruleString) {
        return ProofReason.parseReason(ruleString, MODAL_RULES);
    }

    private static final ModalLogicParser modalParser = new ModalLogicParser();

    public static ModalOperation parseExpression(String expression) {
        return modalParser.evaluate(expression);
    }

    public static AbstractModalAction parseAction(String name, List<Integer> sources, ModalOperation extraInfo, String state) {
        return switch (name) {
            case "Ass" ->
                    extraInfo instanceof RelationOperation relationOperation ? new ModalAssume(relationOperation) : new ModalAssume((ModalLogicalOperation) extraInfo, state);
            case "|I1" -> new ModalOrI1(sources.getFirst(), (ModalLogicalOperation) extraInfo);
            case "|I2" -> new ModalOrI2(sources.getFirst(), (ModalLogicalOperation) extraInfo);
            case "|E" -> new ModalOrE(sources.get(0), sources.get(1), sources.get(2));
            case "&I" -> new ModalAndI(sources.get(0), sources.get(1));
            case "&E1" -> new ModalAndE1(sources.getFirst());
            case "&E2" -> new ModalAndE2(sources.getFirst());
            case "Rep" -> new ModalCopy(sources.getFirst());
            case "-E" -> new ModalNotE(sources.getFirst());
            case "-I" -> new ModalNotI();
            case "->I" -> new ModalDeductionTheorem();
            case "->E" -> new ModalModusPonens(sources.get(0), sources.get(1));
            case "FE" -> new ModalFE(sources.get(0), (ModalLogicalOperation) extraInfo, state);
            case "FI" -> new ModalFI(sources.get(0), sources.get(1));
            case "[]I" -> new ModalBoxI();
            case "[]E" -> new ModalBoxE(sources.get(0), sources.get(1));
            case "<>I" -> new ModalDiaI(sources.get(0), sources.get(1));
            case "<>E" -> new ModalDiaE(sources.get(0));
            case REFL -> new Reflexive(sources.get(0));
            case TRANS -> new Transitive(sources.get(0), sources.get(1));
            default -> throw new IllegalArgumentException("The rule " + name + " is not valid.");
        };
    }
}
