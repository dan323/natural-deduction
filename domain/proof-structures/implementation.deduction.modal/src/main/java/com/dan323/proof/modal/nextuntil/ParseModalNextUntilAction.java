package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.ModalNextUntilLogicParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;

import java.util.Arrays;
import java.util.List;

/**
 * Reads the rules and formulas of {@code modal-next-until}: those of {@link ParseModalAction} plus the Next and Until
 * rules and the rules of linear time. A proof step shows them as {@code XI [i]}, {@code XE [i]}, {@code Succ [i]},
 * {@code UI [i]}, {@code UI [i, j]}, {@code UE [i]}, {@code U<> [i]}, {@code Ind [i, j]}, {@code -U [i]},
 * {@code UW [i]}, {@code UB [i]}, {@code UA [i, j, ...]}, {@code Ord [i, ...]} (or {@code Ord} with no lines),
 * {@code Eq [i, j, ...]} and {@code Lin [i-j]}. A client names them the same way, except that the two Until
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
    public static final String NEGATED_UNTIL = "-U";
    public static final String UNTIL_WITNESS = "UW";
    public static final String UNTIL_WITNESS_RIGHT = "UB";
    public static final String UNTIL_WITNESS_LEFT = "UA";
    public static final String ORDER = "Ord";
    public static final String EQUAL = "Eq";
    public static final String LINEARITY = "Lin";

    private static final List<String> RULE_TEXTS = List.of(NEXT_I, NEXT_E, SUCCESSOR, UNTIL, UNTIL_E, UNTIL_SOMETIME, INDUCTION,
            NEGATED_UNTIL, UNTIL_WITNESS, UNTIL_WITNESS_RIGHT, UNTIL_WITNESS_LEFT, ORDER, EQUAL);
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
        if (ruleString.equals(ORDER)) {
            return new ProofReason(ORDER, List.of(), List.of());
        }
        if (ruleString.startsWith(LINEARITY + " [") && ruleString.endsWith("]")) {
            var range = Arrays.stream(ruleString.substring(LINEARITY.length() + 2, ruleString.length() - 1).split("-"))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
            if (range.length != 2) {
                throw new IllegalArgumentException(LINEARITY + " takes one range of lines");
            }
            return new ProofReason(LINEARITY, List.of(new ProofReason.Range(range[0], range[1])), List.of());
        }
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
            case NEGATED_UNTIL -> new ModalNegatedUntil(lines(reason, 1)[0]);
            case UNTIL_WITNESS -> {
                if (!(atPos instanceof LessEqual relation)) {
                    throw new IllegalArgumentException(UNTIL_WITNESS + " derives a relation");
                }
                yield new ModalUntilWitness(lines(reason, 1)[0], relation.getRight());
            }
            case UNTIL_WITNESS_RIGHT -> new ModalUntilWitnessRight(lines(reason, 1)[0]);
            case UNTIL_WITNESS_LEFT -> {
                int[] lines = atLeast(reason);
                yield new ModalUntilWitnessLeft(lines[0], state, rest(lines));
            }
            case ORDER -> new ModalOrder(Arrays.stream(lines(reason)).boxed().toList(), atPos);
            case EQUAL -> {
                int[] lines = atLeast(reason);
                yield new ModalEqualState(lines[0], state, rest(lines));
            }
            case LINEARITY -> new ModalLinearity();
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
            case NEGATED_UNTIL -> new ModalNegatedUntil(sources.getFirst());
            case UNTIL_WITNESS -> new ModalUntilWitness(sources.getFirst(), state);
            case UNTIL_WITNESS_RIGHT -> new ModalUntilWitnessRight(sources.getFirst());
            case UNTIL_WITNESS_LEFT -> new ModalUntilWitnessLeft(sources.getFirst(), state);
            case ORDER -> new ModalOrder(extraInfo);
            case EQUAL -> new ModalEqualState(sources.getFirst(), state);
            case LINEARITY -> new ModalLinearity();
            default -> ParseModalAction.parseAction(name, sources, extraInfo, state);
        };
    }

    private static int[] lines(ProofReason reason) {
        return reason.getLines().stream().mapToInt(Integer::intValue).toArray();
    }

    private static int[] lines(ProofReason reason, int count) {
        int[] lines = lines(reason);
        if (lines.length != count) {
            throw new IllegalArgumentException(reason.getNameProof() + " takes " + count + (count == 1 ? " line" : " lines"));
        }
        return lines;
    }

    private static int[] atLeast(ProofReason reason) {
        int[] lines = lines(reason);
        if (lines.length == 0) {
            throw new IllegalArgumentException(reason.getNameProof() + " takes at least one line");
        }
        return lines;
    }

    private static List<Integer> rest(int[] lines) {
        return Arrays.stream(lines).skip(1).boxed().toList();
    }
}
