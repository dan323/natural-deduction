package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.ModalNextUntilLogicParser;
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
 * Reads the rules and formulas of {@code modal-next-until}: those of {@link ParseModalAction} plus the Next and Until
 * rules. A proof step shows them as {@code XI [i]}, {@code XE [i]}, {@code Succ [i]}, {@code UI [i]}, {@code UI [i, j]},
 * {@code UE [i]}, {@code U<> [i]} and {@code Ind [i, j]}. A client names them the same way, except that the two Until
 * introductions are {@code UI1} (one line and the left side) and {@code UI2} (two lines).
 */
public final class ParseModalNextUntilAction {

    public static final String NEXT_I = "XI";
    public static final String NEXT_E = "XE";
    public static final String SUCCESSOR = "Succ";
    /** The rule text of both Until introductions. */
    public static final String UNTIL = "UI";
    public static final String UNTIL_I1 = "UI1";
    public static final String UNTIL_I2 = "UI2";
    public static final String UNTIL_E = "UE";
    public static final String UNTIL_SOMETIME = "U<>";
    public static final String INDUCTION = "Ind";

    private static final List<String> RULE_TEXTS = List.of(NEXT_I, NEXT_E, SUCCESSOR, UNTIL, UNTIL_E, UNTIL_SOMETIME, INDUCTION);
    private static final ModalNextUntilLogicParser PARSER = new ModalNextUntilLogicParser();

    private ParseModalNextUntilAction() {
    }

    public static ModalOperation parseExpression(String expression) {
        return PARSER.evaluate(expression);
    }

    /**
     * @return the reason written as {@code ruleString}, or null if no rule is written like that
     */
    public static ProofReason parseReason(String ruleString) {
        for (String rule : RULE_TEXTS) {
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
            case NEXT_I -> new ModalNextI(lines(reason, 1)[0]);
            case NEXT_E -> new ModalNextE(lines(reason, 1)[0]);
            case SUCCESSOR -> new ModalSuccessor(lines(reason, 1)[0]);
            case UNTIL -> {
                int[] lines = lines(reason);
                if (lines.length == 1) {
                    yield new ModalUntilI1(lines[0], (ModalLogicalOperation) ((Until) atPos).getLeft());
                } else if (lines.length == 2) {
                    yield new ModalUntilI2(lines[0], lines[1]);
                }
                throw new IllegalArgumentException(UNTIL + " takes one or two lines");
            }
            case UNTIL_E -> new ModalUntilE(lines(reason, 1)[0]);
            case UNTIL_SOMETIME -> new ModalUntilSometime(lines(reason, 1)[0]);
            case INDUCTION -> {
                int[] lines = lines(reason, 2);
                yield new ModalInduction(lines[0], lines[1]);
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
            case NEXT_I -> new ModalNextI(sources.getFirst());
            case NEXT_E -> new ModalNextE(sources.getFirst());
            case SUCCESSOR -> new ModalSuccessor(sources.getFirst());
            case UNTIL_I1 -> new ModalUntilI1(sources.getFirst(), (ModalLogicalOperation) extraInfo);
            case UNTIL_I2 -> new ModalUntilI2(sources.get(0), sources.get(1));
            case UNTIL_E -> new ModalUntilE(sources.getFirst());
            case UNTIL_SOMETIME -> new ModalUntilSometime(sources.getFirst());
            case INDUCTION -> new ModalInduction(sources.get(0), sources.get(1));
            default -> ParseModalAction.parseAction(name, sources, extraInfo, state);
        };
    }

    private static int[] lines(ProofReason reason) {
        var text = reason.toString();
        return Arrays.stream(text.substring(reason.getNameProof().length() + 2, text.length() - 1).split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static int[] lines(ProofReason reason, int count) {
        int[] lines = lines(reason);
        if (lines.length != count) {
            throw new IllegalArgumentException(reason.getNameProof() + " takes " + count + (count == 1 ? " line" : " lines"));
        }
        return lines;
    }
}
