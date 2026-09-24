package com.dan323.proof.modal.until;

import com.dan323.expressions.ModalUntilLogicParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;

import java.util.Arrays;
import java.util.List;

/**
 * Reads the rules and formulas of modal logic with Until: those of {@link ParseModalAction} plus the Until rules. A
 * proof step shows them as {@code UI [n]} ({@link ModalUntilI1}), {@code UI [n, m]} ({@link ModalUntilI2}),
 * {@code UE [n]} ({@link ModalUntilE1}) and {@code UE [n, m]} ({@link ModalUntilE2}); a client names them
 * {@code UI1}, {@code UI2}, {@code UE1} and {@code UE2}.
 */
public final class ParseModalUntilAction {

    public static final String UNTIL_I1 = "UI1";
    public static final String UNTIL_I2 = "UI2";
    public static final String UNTIL_E1 = "UE1";
    public static final String UNTIL_E2 = "UE2";

    private static final ModalUntilLogicParser PARSER = new ModalUntilLogicParser();

    private ParseModalUntilAction() {
    }

    /**
     * @param state0 the initial state
     * @return an empty proof whose steps are read with these rules
     */
    public static ModalNaturalDeduction newProof(String state0) {
        return new ModalNaturalDeduction(state0, ParseModalUntilAction::parse);
    }

    public static ModalOperation parseExpression(String expression) {
        return PARSER.evaluate(expression);
    }

    /**
     * @return the reason written as {@code ruleString}, or null if no rule is written like that
     */
    public static ProofReason parseReason(String ruleString) {
        for (String rule : List.of(UntilSteps.UNTIL_I, UntilSteps.UNTIL_E)) {
            if (ruleString.startsWith(rule + " [") && ruleString.endsWith("]")) {
                var lines = Arrays.stream(ruleString.substring(rule.length() + 2, ruleString.length() - 1).split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .toList();
                return new ProofReason(rule, List.of(), lines);
            }
        }
        return ParseModalAction.parseReason(ruleString);
    }

    public static AbstractModalAction parseWithReason(ModalNaturalDeduction proof, ModalOperation atPos, ProofReason reason, String state) {
        return switch (reason.getNameProof()) {
            case UntilSteps.UNTIL_I -> {
                int[] lines = lines(reason);
                yield lines.length == 1
                        ? new ModalUntilI1(lines[0], (ModalLogicalOperation) ((Until) atPos).getLeft())
                        : new ModalUntilI2(lines[0], lines[1]);
            }
            case UntilSteps.UNTIL_E -> {
                int[] lines = lines(reason);
                yield lines.length == 1 ? new ModalUntilE1(lines[0]) : new ModalUntilE2(lines[0], lines[1]);
            }
            default -> ParseModalAction.parseWithReason(proof, atPos, reason, state);
        };
    }

    public static AbstractModalAction parse(ModalNaturalDeduction proof, int pos) {
        var step = proof.getSteps().get(pos - 1);
        return parseWithReason(proof, step.getStep(), step.getProof(), step.getState());
    }

    public static AbstractModalAction parseAction(String name, List<Integer> sources, ModalOperation extraInfo, String state) {
        return switch (name) {
            case UNTIL_I1 -> new ModalUntilI1(sources.getFirst(), (ModalLogicalOperation) extraInfo);
            case UNTIL_I2 -> new ModalUntilI2(sources.get(0), sources.get(1));
            case UNTIL_E1 -> new ModalUntilE1(sources.getFirst());
            case UNTIL_E2 -> new ModalUntilE2(sources.get(0), sources.get(1));
            default -> ParseModalAction.parseAction(name, sources, extraInfo, state);
        };
    }

    private static int[] lines(ProofReason reason) {
        var text = reason.toString();
        int[] lines = Arrays.stream(text.substring(reason.getNameProof().length() + 2, text.length() - 1).split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
        if (lines.length != 1 && lines.length != 2) {
            throw new IllegalArgumentException(reason.getNameProof() + " takes one or two lines");
        }
        return lines;
    }
}
