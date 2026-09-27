package com.dan323.proof.firstorder.proof;

import com.dan323.expressions.firstorder.ConjunctionFirstOrder;
import com.dan323.expressions.firstorder.DisjunctionFirstOrder;
import com.dan323.expressions.firstorder.Equals;
import com.dan323.expressions.firstorder.Exists;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.FirstOrderParser;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.expressions.firstorder.Term;
import com.dan323.expressions.firstorder.VariableTerm;
import com.dan323.proof.firstorder.*;
import com.dan323.proof.generic.proof.ProofReason;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Reads the rules of first-order proofs: back from a proof's steps ({@link #parse}, {@link #parseReason}) and from a
 * rule name with its parameters ({@link #parseAction}).
 */
public final class ParseFirstOrderAction {

    private static final FirstOrderParser PARSER = new FirstOrderParser();

    private ParseFirstOrderAction() {
    }

    public static FirstOrderAction parse(FirstOrderNaturalDeduction proof, int pos) {
        var step = proof.getSteps().get(pos - 1);
        return parseWithReason(proof, step.getStep(), step.getProof());
    }

    /**
     * @param proof       the proof the step belongs to
     * @param atPos       the formula the step derived
     * @param proofReason the rule and lines that derived it
     * @return the action that derives {@code atPos} with {@code proofReason}
     * @throws IllegalArgumentException if the step cannot come from that rule
     */
    public static FirstOrderAction parseWithReason(FirstOrderNaturalDeduction proof, FirstOrderOperation atPos, ProofReason proofReason) {
        return switch (proofReason.getNameProof()) {
            case "Ass" -> new FirstOrderAssume(atPos);
            case "|I" -> parseOrI(proof, atPos, proofReason);
            case "|E" -> {
                int[] ints = parseArray(proofReason);
                yield new FirstOrderOrE(ints[0], ints[1], ints[2]);
            }
            case "&I" -> {
                int[] ints = parseArray(proofReason);
                yield new FirstOrderAndI(ints[0], ints[1]);
            }
            case "&E" -> parseAndE(proof, atPos, proofReason);
            case "Rep" -> new FirstOrderCopy(parseArray(proofReason)[0]);
            case "-E" -> new FirstOrderNotE(parseArray(proofReason)[0]);
            case "-I" -> new FirstOrderNotI();
            case "->I" -> new FirstOrderDeductionTheorem();
            case "->E" -> {
                int[] ints = parseArray(proofReason);
                yield new FirstOrderModusPonens(ints[0], ints[1]);
            }
            case "FE" -> new FirstOrderFE(parseArray(proofReason)[0], atPos);
            case "FI" -> {
                int[] ints = parseArray(proofReason);
                yield new FirstOrderFI(ints[0], ints[1]);
            }
            case FirstOrderForallE.NAME -> parseForallE(proof, atPos, proofReason);
            case FirstOrderForallI.NAME -> new FirstOrderForallI(parseArray(proofReason)[0], cast(atPos, Forall.class));
            case FirstOrderExistsI.NAME -> new FirstOrderExistsI(parseArray(proofReason)[0], cast(atPos, Exists.class));
            case FirstOrderExistsE.NAME -> new FirstOrderExistsE(parseArray(proofReason)[0]);
            case FirstOrderEqualsI.NAME -> new FirstOrderEqualsI(cast(atPos, Equals.class).left());
            case FirstOrderEqualsE.NAME -> {
                int[] ints = parseArray(proofReason);
                yield new FirstOrderEqualsE(ints[0], ints[1], atPos);
            }
            default -> throw new IllegalArgumentException("The rule " + proofReason.getNameProof() + " is not valid.");
        };
    }

    private static <F extends FirstOrderOperation> F cast(FirstOrderOperation formula, Class<F> kind) {
        if (kind.isInstance(formula)) {
            return kind.cast(formula);
        }
        throw new IllegalArgumentException(formula + " is not a " + kind.getSimpleName());
    }

    private static FirstOrderAction parseOrI(FirstOrderNaturalDeduction proof, FirstOrderOperation atPos, ProofReason proofReason) {
        int line = parseArray(proofReason)[0];
        FirstOrderOperation origin = proof.getSteps().get(line - 1).getStep();
        DisjunctionFirstOrder disjunction = cast(atPos, DisjunctionFirstOrder.class);
        if (disjunction.getLeft().equals(origin)) {
            return new FirstOrderOrI1(line, disjunction.getRight());
        } else {
            return new FirstOrderOrI2(line, disjunction.getLeft());
        }
    }

    private static FirstOrderAction parseAndE(FirstOrderNaturalDeduction proof, FirstOrderOperation atPos, ProofReason proofReason) {
        int line = parseArray(proofReason)[0];
        ConjunctionFirstOrder conjunction = cast(proof.getSteps().get(line - 1).getStep(), ConjunctionFirstOrder.class);
        if (atPos.equals(conjunction.getLeft())) {
            return new FirstOrderAndE1(line);
        } else {
            return new FirstOrderAndE2(line);
        }
    }

    /**
     * The term of a {@code ∀E} step is not written down: it is the term that makes the step an instance of the body.
     */
    private static FirstOrderAction parseForallE(FirstOrderNaturalDeduction proof, FirstOrderOperation atPos, ProofReason proofReason) {
        int line = parseArray(proofReason)[0];
        Forall forall = cast(proof.getSteps().get(line - 1).getStep(), Forall.class);
        var match = Instances.instanceOf(forall.getBody(), forall.getVariable(), atPos);
        if (!match.matches()) {
            throw new IllegalArgumentException(atPos + " is not an instance of " + forall);
        }
        return new FirstOrderForallE(line, match.isVacuous() ? new VariableTerm(forall.getVariable()) : match.term());
    }

    private static int[] parseArray(ProofReason proofReason) {
        String reason = proofReason.toString();
        return Arrays.stream(reason.substring(proofReason.getNameProof().length() + 2, reason.length() - 1).split(","))
                .map(String::trim)
                .filter(split -> !split.contains("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static int[] parseLines(String ruleString, int nameLength) {
        return Arrays.stream(ruleString.substring(nameLength + 2, ruleString.length() - 1).split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static ProofReason lines(String name, String ruleString) {
        return new ProofReason(name, List.of(), Arrays.stream(parseLines(ruleString, name.length())).boxed().toList());
    }

    private static ProofReason existsE(String ruleString) {
        String[] parts = Arrays.stream(ruleString.substring(FirstOrderExistsE.NAME.length() + 2, ruleString.length() - 1).split(","))
                .map(String::trim)
                .toArray(String[]::new);
        int[] range = Arrays.stream(parts[1].split("-")).map(String::trim).mapToInt(Integer::parseInt).toArray();
        return new ProofReason(FirstOrderExistsE.NAME, List.of(new ProofReason.Range(range[0], range[1])),
                List.of(Integer.parseInt(parts[0])));
    }

    /**
     * {@link ProofReason#parseReason} tells the rules apart by their first three characters.
     */
    private static final Map<String, Function<String, ProofReason>> FIRST_ORDER_RULES = Map.of(
            FirstOrderForallE.NAME + " ", st -> lines(FirstOrderForallE.NAME, st),
            FirstOrderForallI.NAME + " ", st -> lines(FirstOrderForallI.NAME, st),
            FirstOrderExistsI.NAME + " ", st -> lines(FirstOrderExistsI.NAME, st),
            FirstOrderExistsE.NAME + " ", ParseFirstOrderAction::existsE,
            FirstOrderEqualsE.NAME + " ", st -> lines(FirstOrderEqualsE.NAME, st));

    /**
     * @param ruleString the rule part of a line of proof text, such as {@code ∀E [1]}, {@code ∃E [1, 2-4]} or {@code =I}
     * @return the reason it denotes
     * @throws IllegalArgumentException if it is not a rule
     */
    public static ProofReason parseReason(String ruleString) {
        String rule = ruleString.strip();
        if (FirstOrderEqualsI.NAME.equals(rule)) {
            return new ProofReason(FirstOrderEqualsI.NAME, List.of(), List.of());
        }
        if (rule.length() < 3) {
            throw new IllegalArgumentException("Not a valid rule");
        }
        ProofReason reason = ProofReason.parseReason(rule, FIRST_ORDER_RULES);
        if (reason == null) {
            throw new IllegalArgumentException("Not a valid rule");
        }
        return reason;
    }

    /**
     * @param expression a formula
     * @return the formula {@code expression} denotes
     * @throws IllegalArgumentException if it is not a formula
     */
    public static FirstOrderOperation parseExpression(String expression) {
        return PARSER.parse(expression);
    }

    /**
     * @param term a term
     * @return the term {@code term} denotes
     * @throws IllegalArgumentException if it is not a term
     */
    public static Term parseTerm(String term) {
        return PARSER.parseTerm(term);
    }

    /**
     * The names classical logic gives to the rules it shares with first-order logic, mapped to the rule names, so that
     * a client can send the same name to every logic (as {@code ParseModalAction.ruleName} does).
     */
    private static final Map<String, String> CLASSICAL_NAMES = Map.ofEntries(
            Map.entry("ASSUME", "Ass"), Map.entry("ORI1", "|I1"), Map.entry("ORI2", "|I2"), Map.entry("ORE", "|E"),
            Map.entry("ANDI", "&I"), Map.entry("ANDE1", "&E1"), Map.entry("ANDE2", "&E2"), Map.entry("COPY", "Rep"),
            Map.entry("NOTE", "-E"), Map.entry("NOTI", "-I"), Map.entry("DT", "->I"), Map.entry("MP", "->E"));

    /**
     * @return the first-order rule name for {@code name}: {@code name} itself, unless it is the classical name of a
     * shared rule (e.g. {@code COPY} gives {@code Rep})
     */
    public static String ruleName(String name) {
        return CLASSICAL_NAMES.getOrDefault(name, name);
    }

    /**
     * @param name       the rule name ({@code ∀E}, {@code Rep}, ...) or the classical name of a shared rule
     * @param sources    the 1-based lines the rule uses, in order
     * @param expression the formula the rule needs (the assumption, the added disjunct, the target of {@code ∀I},
     *                   {@code ∃I} or {@code =E}), or {@code null}
     * @param term       the term of {@code ∀E} and {@code =I}, or {@code null}
     * @return the action; a target of the wrong kind gives an action that is not valid
     * @throws IllegalArgumentException if there is no rule called {@code name}
     */
    public static FirstOrderAction parseAction(String name, List<Integer> sources, FirstOrderOperation expression, Term term) {
        return switch (ruleName(name)) {
            case "Ass" -> new FirstOrderAssume(expression);
            case "|I1" -> new FirstOrderOrI1(sources.getFirst(), expression);
            case "|I2" -> new FirstOrderOrI2(sources.getFirst(), expression);
            case "|E" -> new FirstOrderOrE(sources.get(0), sources.get(1), sources.get(2));
            case "&I" -> new FirstOrderAndI(sources.get(0), sources.get(1));
            case "&E1" -> new FirstOrderAndE1(sources.getFirst());
            case "&E2" -> new FirstOrderAndE2(sources.getFirst());
            case "Rep" -> new FirstOrderCopy(sources.getFirst());
            case "-E" -> new FirstOrderNotE(sources.getFirst());
            case "-I" -> new FirstOrderNotI();
            case "->I" -> new FirstOrderDeductionTheorem();
            case "->E" -> new FirstOrderModusPonens(sources.get(0), sources.get(1));
            case "FE" -> new FirstOrderFE(sources.getFirst(), expression);
            case "FI" -> new FirstOrderFI(sources.get(0), sources.get(1));
            case FirstOrderForallE.NAME -> new FirstOrderForallE(sources.getFirst(), term);
            case FirstOrderForallI.NAME ->
                    new FirstOrderForallI(sources.getFirst(), expression instanceof Forall forall ? forall : null);
            case FirstOrderExistsI.NAME ->
                    new FirstOrderExistsI(sources.getFirst(), expression instanceof Exists exists ? exists : null);
            case FirstOrderExistsE.NAME -> new FirstOrderExistsE(sources.getFirst());
            case FirstOrderEqualsI.NAME -> new FirstOrderEqualsI(term);
            case FirstOrderEqualsE.NAME -> new FirstOrderEqualsE(sources.get(0), sources.get(1), expression);
            default -> throw new IllegalArgumentException("The rule " + name + " is not valid.");
        };
    }
}
