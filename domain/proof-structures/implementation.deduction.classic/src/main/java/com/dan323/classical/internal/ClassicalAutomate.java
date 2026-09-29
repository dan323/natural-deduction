package com.dan323.classical.internal;

import com.dan323.classical.*;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static com.dan323.classical.internal.ClassicalSteps.FALSE;
import static com.dan323.classical.internal.ClassicalSteps.checkNotInterrupted;

/**
 * The automatic solver of classical logic.
 *
 * <p>It first looks for a proof of the sequent {@code premises ⇒ goal} in the sequent calculus G3cp, in its one-sided
 * form: an analytic tableau that refutes the set {@code premises, - goal}. G3cp is complete for classical propositional
 * logic and all its rules are invertible, so the search never backtracks; every rule replaces a formula by smaller
 * ones, so it terminates. It can still take exponential time (each branching rule doubles the work), so a large goal
 * may run into the caller's solve timeout. {@code FALSE} closes a branch; {@code TRUE} has no rule of its own (natural
 * deduction cannot introduce it), so it is treated as an atom. The rule applications that the closed tableau does not
 * need are then pruned.
 *
 * <p>When there is a closed tableau it is translated into natural deduction steps. The rules applied to
 * {@code - goal} become introduction rules for the goal ({@code ->I}, {@code -I}, {@code &I}, {@code |I1/2}) as long as
 * the rest of the tableau does not need {@code - goal} itself; otherwise the translation assumes {@code - goal},
 * derives {@code FALSE}, and ends with {@code -I} and {@code -E}. When there is no closed tableau the proof is left with
 * its premises.
 *
 * @author daniel
 */
public final class ClassicalAutomate {

    private final Map<Tableau, Set<ClassicalLogicOperation>> references = new IdentityHashMap<>();
    private ClassicalSteps steps;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public ClassicalAutomate() {
        // Nothing to set up: automate initializes the state
    }

    /**
     * Restarts the proof from its premises and finishes it if the goal follows from them. Otherwise the proof is left
     * with its premises only.
     *
     * <p>It checks the interrupt flag of the calling thread during the search: interrupting the thread stops it with a
     * {@link CancellationException}.
     *
     * @param naturalDeduction the proof to solve
     * @throws CancellationException if the calling thread is interrupted
     */
    public void automate(NaturalDeduction naturalDeduction) {
        naturalDeduction.reset();
        steps = new ClassicalSteps(naturalDeduction, "classical");
        references.clear();
        ClassicalLogicOperation goal = naturalDeduction.getGoal();
        var set = new ArrayList<ClassicalLogicOperation>();
        if (!goal.equals(FALSE)) {
            set.add(negate(goal));
        }
        naturalDeduction.getAssms().forEach(premise -> addIfAbsent(set, premise));
        Tableau tableau = search(set);
        if (tableau == null) {
            return;
        }
        tableau = prune(tableau);
        Map<ClassicalLogicOperation, Integer> context = new HashMap<>();
        for (int i = 0; i < naturalDeduction.getSteps().size(); i++) {
            context.putIfAbsent(naturalDeduction.getSteps().get(i).getStep(), i + 1);
        }
        int line = derive(context, goal, tableau);
        if (line != steps.lastLine()) {
            steps.apply(new ClassicCopy(line));
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Search: a tableau for G3cp
    // ------------------------------------------------------------------------------------------------------------

    /**
     * A closed tableau: each record is the rule applied to the set of formulas of its branch, with the tableaux of the
     * branches it leads to.
     */
    private sealed interface Tableau {
    }

    /** {@code formula} and {@code - formula} are in the set. */
    private record Closed(ClassicalLogicOperation formula) implements Tableau {
    }

    /** {@code FALSE} is in the set. */
    private record Falsum() implements Tableau {
    }

    /** {@code principal} is replaced by both its {@link #alpha components}. */
    private record Alpha(ClassicalLogicOperation principal, Tableau rest) implements Tableau {
    }

    /** {@code principal} is replaced by one of its {@link #beta components} in each branch. */
    private record Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) implements Tableau {
    }

    private Tableau search(List<ClassicalLogicOperation> set) {
        checkNotInterrupted();
        if (set.contains(FALSE)) {
            return new Falsum();
        }
        for (ClassicalLogicOperation formula : set) {
            if (formula instanceof NegationClassic negation && set.contains(negation.getElement())) {
                return new Closed(negation.getElement());
            }
        }
        ClassicalLogicOperation principal = null;
        for (ClassicalLogicOperation formula : set) {
            if (priority(formula) < priority(principal)) {
                principal = formula;
            }
        }
        if (principal == null) {
            return null;
        }
        List<ClassicalLogicOperation> alpha = alpha(principal);
        if (alpha != null) {
            Tableau rest = search(replace(set, principal, alpha));
            return rest == null ? null : new Alpha(principal, rest);
        }
        List<ClassicalLogicOperation> beta = beta(principal);
        Tableau left = search(replace(set, principal, List.of(beta.get(0))));
        Tableau right = left == null ? null : search(replace(set, principal, List.of(beta.get(1))));
        return right == null ? null : new Beta(principal, left, right);
    }

    /**
     * The order in which the rules are applied, lowest first. Every rule is invertible, so it only shapes the proof:
     * cases on a disjunction come before the rule of {@code - (A | B)}, which the translation can seldom turn into an
     * introduction rule. {@link Integer#MAX_VALUE} for a formula without a rule, and for no formula.
     */
    private static int priority(ClassicalLogicOperation formula) {
        if (formula == null) {
            return Integer.MAX_VALUE;
        }
        if (alpha(formula) != null) {
            boolean negatedDisjunction = formula instanceof NegationClassic negation && negation.getElement() instanceof DisjunctionClassic;
            return negatedDisjunction ? 2 : 0;
        }
        if (beta(formula) != null) {
            return formula instanceof DisjunctionClassic ? 1 : 3;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * The two formulas that replace {@code formula} together: {@code A & B} gives {@code A} and {@code B};
     * {@code - (A | B)} gives {@code - A} and {@code - B}; {@code - (A -> B)} gives {@code A} and {@code - B};
     * {@code - (- A)} gives {@code A} (twice). Null when {@code formula} is not of these kinds.
     */
    private static List<ClassicalLogicOperation> alpha(ClassicalLogicOperation formula) {
        if (formula instanceof ConjunctionClassic conjunction) {
            return List.of(conjunction.getLeft(), conjunction.getRight());
        }
        if (formula instanceof NegationClassic negation) {
            if (negation.getElement() instanceof DisjunctionClassic disjunction) {
                return List.of(negate(disjunction.getLeft()), negate(disjunction.getRight()));
            }
            if (negation.getElement() instanceof ImplicationClassic implication) {
                return List.of(implication.getLeft(), negate(implication.getRight()));
            }
            if (negation.getElement() instanceof NegationClassic inner) {
                return List.of(inner.getElement(), inner.getElement());
            }
        }
        return null;
    }

    /**
     * The two formulas, one for each branch, that replace {@code formula}: {@code A | B} gives {@code A} and
     * {@code B}; {@code A -> B} gives {@code - A} and {@code B}; {@code - (A & B)} gives {@code - A} and {@code - B}.
     * Null when {@code formula} is not of these kinds.
     */
    private static List<ClassicalLogicOperation> beta(ClassicalLogicOperation formula) {
        if (formula instanceof DisjunctionClassic disjunction) {
            return List.of(disjunction.getLeft(), disjunction.getRight());
        }
        if (formula instanceof ImplicationClassic implication) {
            return List.of(negate(implication.getLeft()), implication.getRight());
        }
        if (formula instanceof NegationClassic negation && negation.getElement() instanceof ConjunctionClassic conjunction) {
            return List.of(negate(conjunction.getLeft()), negate(conjunction.getRight()));
        }
        return null;
    }

    private static NegationClassic negate(ClassicalLogicOperation formula) {
        return new NegationClassic(formula);
    }

    /**
     * {@code set} with {@code principal} replaced, in its place, by the {@code added} formulas that are not in it yet.
     */
    private static List<ClassicalLogicOperation> replace(List<ClassicalLogicOperation> set, ClassicalLogicOperation principal,
                                                         List<ClassicalLogicOperation> added) {
        var result = new ArrayList<ClassicalLogicOperation>(set.size() + added.size());
        for (ClassicalLogicOperation formula : set) {
            if (formula.equals(principal)) {
                added.stream().filter(component -> !set.contains(component)).forEach(component -> addIfAbsent(result, component));
            } else {
                result.add(formula);
            }
        }
        return result;
    }

    private static void addIfAbsent(List<ClassicalLogicOperation> set, ClassicalLogicOperation formula) {
        if (!set.contains(formula)) {
            set.add(formula);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Pruning
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Drops the rule applications whose new formulas the rest of the tableau never uses, and records in
     * {@link #references} the formulas that each remaining tableau uses (a superset: it counts the formulas a rule adds
     * as well).
     */
    private Tableau prune(Tableau tableau) {
        return switch (tableau) {
            case Closed(ClassicalLogicOperation formula) -> referencing(tableau, Set.of(formula, negate(formula)));
            case Falsum() -> referencing(tableau, Set.of(FALSE));
            case Alpha(ClassicalLogicOperation principal, Tableau rest) -> {
                Tableau pruned = prune(rest);
                if (alpha(principal).stream().noneMatch(references(pruned)::contains)) {
                    yield pruned;
                }
                yield referencing(new Alpha(principal, pruned), references(pruned), Set.of(principal));
            }
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) -> {
                List<ClassicalLogicOperation> beta = beta(principal);
                Tableau leftPruned = prune(left);
                if (!references(leftPruned).contains(beta.get(0))) {
                    yield leftPruned;
                }
                Tableau rightPruned = prune(right);
                if (!references(rightPruned).contains(beta.get(1))) {
                    yield rightPruned;
                }
                yield referencing(new Beta(principal, leftPruned, rightPruned), references(leftPruned),
                        references(rightPruned), Set.of(principal));
            }
        };
    }

    @SafeVarargs
    private Tableau referencing(Tableau tableau, Set<ClassicalLogicOperation>... formulas) {
        var all = new HashSet<ClassicalLogicOperation>();
        for (Set<ClassicalLogicOperation> set : formulas) {
            all.addAll(set);
        }
        references.put(tableau, all);
        return tableau;
    }

    private Set<ClassicalLogicOperation> references(Tableau tableau) {
        return references.get(tableau);
    }

    private boolean mentions(Tableau tableau, ClassicalLogicOperation formula) {
        return references(tableau).contains(formula);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Translation into natural deduction
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Writes the steps that derive {@code target} following {@code tableau}.
     *
     * <p>When {@code target} is {@code FALSE}, every formula of the branch is in {@code context}. Otherwise the branch
     * also has {@code - target}, which has no line: the translation goes for {@code target} itself, and assumes
     * {@code - target} only when the tableau needs it.
     *
     * @param context the line (1-based) of each formula of the branch, all of them valid and in scope; it may gain the
     *                formulas the tableau adds to the branch
     * @return the line of {@code target}, in the current scope
     */
    private int derive(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target, Tableau tableau) {
        if (context.containsKey(target)) {
            return context.get(target);
        }
        ClassicalLogicOperation negated = target.equals(FALSE) ? null : negate(target);
        if (negated != null && context.containsKey(negated)) {
            return fromFalse(derive(context, FALSE, tableau), target);
        }
        if (negated != null && context.containsKey(negate(negated))) {
            return steps.apply(new ClassicNotE(context.get(negate(negated))));
        }
        if (negated != null && needsNegatedTarget(tableau, negated)) {
            return byContradiction(context, target, inner -> derive(inner, FALSE, tableau));
        }
        return switch (tableau) {
            case Falsum() -> fromFalse(steps.line(context, FALSE), target);
            case Closed(ClassicalLogicOperation formula) ->
                    fromFalse(steps.apply(new ClassicFI(steps.line(context, formula), steps.line(context, negate(formula)))), target);
            case Alpha(ClassicalLogicOperation principal, Tableau rest) when principal.equals(negated) ->
                    introduce(context, target, rest);
            case Alpha(ClassicalLogicOperation principal, Tableau rest) -> {
                addAlpha(context, principal, references(rest));
                yield derive(context, target, rest);
            }
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) when principal.equals(negated) -> {
                // - (A & B) for the target A & B
                var conjunction = (ConjunctionClassic) target;
                int leftLine = derive(new HashMap<>(context), conjunction.getLeft(), left);
                int rightLine = derive(new HashMap<>(context), conjunction.getRight(), right);
                yield steps.apply(new ClassicAndI(leftLine, rightLine));
            }
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) ->
                    beta(context, target, principal, left, right);
        };
    }

    /**
     * Whether translating the node {@code tableau} for the target {@code - negated} needs the line of {@code negated}:
     * when it closes with it, or when it goes on with another target (whose translation cannot use {@code negated})
     * and the tableau there uses it.
     */
    private boolean needsNegatedTarget(Tableau tableau, ClassicalLogicOperation negated) {
        return switch (tableau) {
            case Falsum() -> false;
            case Closed(ClassicalLogicOperation formula) -> formula.equals(negated);
            case Alpha(ClassicalLogicOperation principal, Tableau rest) when principal.equals(negated) -> {
                List<ClassicalLogicOperation> components = alpha(principal);
                // - (A | B) goes on with the target A or B, so the rest must not use the other one
                boolean both = ((NegationClassic) negated).getElement() instanceof DisjunctionClassic
                        && !components.get(0).equals(components.get(1))
                        && mentions(rest, components.get(0)) && mentions(rest, components.get(1));
                yield both || mentions(rest, negated);
            }
            case Alpha alpha -> false;
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) when principal instanceof DisjunctionClassic ->
                    false;
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) when principal instanceof ImplicationClassic ->
                    mentions(left, negated);
            case Beta(ClassicalLogicOperation principal, Tableau left, Tableau right) ->
                    mentions(left, negated) || mentions(right, negated);
        };
    }

    /**
     * The rule of the tableau applied to {@code - target}, turned into an introduction rule for {@code target}.
     */
    private int introduce(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target, Tableau rest) {
        return switch (target) {
            case ImplicationClassic implication ->
                    steps.hypothetical(context, target, inner -> derive(inner, implication.getRight(), rest));
            case NegationClassic negation -> steps.hypothetical(context, target, inner -> derive(inner, FALSE, rest));
            case DisjunctionClassic disjunction when mentions(rest, negate(disjunction.getLeft())) ->
                    steps.apply(new ClassicOrI1(derive(context, disjunction.getLeft(), rest), disjunction.getRight()));
            case DisjunctionClassic disjunction ->
                    steps.apply(new ClassicOrI2(derive(context, disjunction.getRight(), rest), disjunction.getLeft()));
            default -> throw new IllegalStateException("No introduction rule for " + target);
        };
    }

    /**
     * Adds to {@code context} the components of {@code principal} (a formula with an {@link #alpha} rule) that are in
     * {@code used}.
     */
    private void addAlpha(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation principal,
                          Set<ClassicalLogicOperation> used) {
        int line = steps.line(context, principal);
        List<ClassicalLogicOperation> components = alpha(principal);
        ClassicalLogicOperation first = components.get(0);
        ClassicalLogicOperation second = components.get(1);
        if (principal instanceof ConjunctionClassic) {
            addIfUsed(context, used, first, () -> steps.apply(new ClassicAndE1(line)));
            addIfUsed(context, used, second, () -> steps.apply(new ClassicAndE2(line)));
            return;
        }
        switch (((NegationClassic) principal).getElement()) {
            case DisjunctionClassic disjunction -> {
                // - A: A gives A | B, which contradicts - (A | B); likewise - B
                addIfUsed(context, used, first, () -> steps.hypothetical(context, first, inner -> steps.apply(new ClassicFI(
                        steps.apply(new ClassicOrI1(steps.line(inner, disjunction.getLeft()), disjunction.getRight())), line))));
                addIfUsed(context, used, second, () -> steps.hypothetical(context, second, inner -> steps.apply(new ClassicFI(
                        steps.apply(new ClassicOrI2(steps.line(inner, disjunction.getRight()), disjunction.getLeft())), line))));
            }
            case ImplicationClassic implication -> {
                // A: with - A, A -> B holds (A gives FALSE), which contradicts - (A -> B)
                addIfUsed(context, used, first, () -> byContradiction(context, first, inner -> steps.apply(new ClassicFI(
                        steps.hypothetical(inner, implication, deeper -> steps.apply(new ClassicFE(steps.apply(new ClassicFI(
                                steps.line(deeper, first), steps.line(deeper, negate(first)))), implication.getRight()))),
                        line))));
                // - B: B gives A -> B, which contradicts - (A -> B)
                addIfUsed(context, used, second, () -> steps.hypothetical(context, second, inner -> steps.apply(new ClassicFI(
                        steps.hypothetical(inner, implication, deeper -> steps.line(deeper, implication.getRight())), line))));
            }
            default -> addIfUsed(context, used, first, () -> steps.apply(new ClassicNotE(line)));
        }
    }

    private static void addIfUsed(Map<ClassicalLogicOperation, Integer> context, Set<ClassicalLogicOperation> used,
                                  ClassicalLogicOperation formula, IntSupplier line) {
        if (used.contains(formula) && !context.containsKey(formula)) {
            context.put(formula, line.getAsInt());
        }
    }

    /**
     * The rule of the tableau applied to {@code principal}, a formula with a {@link #beta} rule that is not
     * {@code - target}.
     */
    private int beta(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target,
                     ClassicalLogicOperation principal, Tableau left, Tableau right) {
        int line = steps.line(context, principal);
        return switch (principal) {
            case DisjunctionClassic disjunction -> {
                int leftCase = steps.hypothetical(context, new ImplicationClassic(disjunction.getLeft(), target),
                        inner -> derive(inner, target, left));
                int rightCase = steps.hypothetical(context, new ImplicationClassic(disjunction.getRight(), target),
                        inner -> derive(inner, target, right));
                yield steps.apply(new ClassicOrE(line, leftCase, rightCase));
            }
            case ImplicationClassic implication -> {
                int antecedent = derive(new HashMap<>(context), implication.getLeft(), left);
                context.put(implication.getRight(), steps.apply(new ClassicModusPonens(line, antecedent)));
                yield derive(context, target, right);
            }
            default -> {
                // - (A & B): A & B contradicts it
                var conjunction = (ConjunctionClassic) ((NegationClassic) principal).getElement();
                int leftLine = derive(new HashMap<>(context), conjunction.getLeft(), left);
                int rightLine = derive(new HashMap<>(context), conjunction.getRight(), right);
                yield fromFalse(steps.apply(new ClassicFI(steps.apply(new ClassicAndI(leftLine, rightLine)), line)), target);
            }
        };
    }

    /**
     * Proves {@code target} by contradiction: assumes {@code - target}, runs {@code body} (which gives the line of
     * {@code FALSE}), then {@code -I} gives {@code - (- target)} and {@code -E} gives {@code target}.
     *
     * @return the line of {@code target}
     */
    private int byContradiction(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target,
                                ToIntFunction<Map<ClassicalLogicOperation, Integer>> body) {
        int doubleNegation = steps.hypothetical(context, negate(negate(target)), body);
        return steps.apply(new ClassicNotE(doubleNegation));
    }

    /**
     * {@code target} from {@code FALSE} at line {@code falseLine}: that line itself when {@code target} is
     * {@code FALSE}, otherwise {@code FE}.
     */
    private int fromFalse(int falseLine, ClassicalLogicOperation target) {
        return target.equals(FALSE) ? falseLine : steps.apply(new ClassicFE(falseLine, target));
    }
}
