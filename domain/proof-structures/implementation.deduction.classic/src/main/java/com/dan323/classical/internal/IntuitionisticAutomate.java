package com.dan323.classical.internal;

import com.dan323.classical.*;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

/**
 * The automatic solver of intuitionistic logic.
 *
 * <p>It first looks for a proof of the sequent {@code premises ⇒ goal} in Dyckhoff's contraction-free sequent calculus
 * G4ip, which is complete for intuitionistic propositional logic and whose backward search always terminates: every
 * rule replaces its principal formula by smaller ones (in the multiset ordering of G4ip's weights). A negation
 * {@code - A} is read as {@code A -> FALSE}; {@code TRUE} has no rule of its own (natural deduction cannot introduce it),
 * so it is treated as an atom.
 *
 * <p>When a sequent proof exists it is translated into natural deduction steps that only use {@code Ass}, {@code ->I},
 * {@code ->E}, {@code &I}, {@code &E1/2}, {@code |I1/2}, {@code |E}, {@code -I}, {@code FI}, {@code FE} and
 * {@code Rep}: never double negation elimination ({@code -E}). When there is none the proof is left with its premises.
 *
 * @author daniel
 */
public final class IntuitionisticAutomate {

    private static final ClassicalLogicOperation FALSE = ConstantClassic.FALSE;

    private final Set<Sequent> unprovable = new HashSet<>();
    private NaturalDeduction proof;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public IntuitionisticAutomate() {
        // Nothing to set up: automate initializes the state
    }

    /**
     * Restarts the proof from its premises and finishes it if the goal follows from them intuitionistically. Otherwise
     * the proof is left with its premises only.
     *
     * <p>It checks the interrupt flag of the calling thread during the search: interrupting the thread stops it with a
     * {@link CancellationException}.
     *
     * @param naturalDeduction the proof to solve
     * @throws CancellationException if the calling thread is interrupted
     */
    public void automate(NaturalDeduction naturalDeduction) {
        proof = naturalDeduction;
        proof.reset();
        unprovable.clear();
        ClassicalLogicOperation goal = proof.getGoal();
        Derivation derivation = search(distinct(proof.getAssms()), goal);
        if (derivation == null) {
            return;
        }
        Map<ClassicalLogicOperation, Integer> context = new HashMap<>();
        for (int i = 0; i < proof.getSteps().size(); i++) {
            context.putIfAbsent(proof.getSteps().get(i).getStep(), i + 1);
        }
        int line = derive(context, goal, derivation);
        if (line != lastLine()) {
            new ClassicCopy(line).apply(proof);
        }
    }

    private static void checkNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("The automatic solver was interrupted");
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Search in G4ip
    // ------------------------------------------------------------------------------------------------------------

    /**
     * A sequent {@code gamma ⇒ goal}, as a key of the cache of the sequents that have no proof.
     */
    private record Sequent(Set<ClassicalLogicOperation> gamma, ClassicalLogicOperation goal) {
    }

    /**
     * The antecedent and consequent of an implication, or of a negation read as an implication to {@code FALSE}.
     */
    private record Arrow(ClassicalLogicOperation antecedent, ClassicalLogicOperation consequent) {
    }

    /**
     * A proof of a sequent in G4ip: each record is the last rule, with the proofs of its premises.
     */
    private sealed interface Derivation {
    }

    /** The goal is in the context. */
    private record Axiom() implements Derivation {
    }

    /** {@code FALSE} is in the context. */
    private record ExFalso() implements Derivation {
    }

    /** The goal is an implication or a negation: assume its antecedent. */
    private record ArrowRight(Derivation body) implements Derivation {
    }

    private record AndRight(Derivation left, Derivation right) implements Derivation {
    }

    private record OrRight1(Derivation left) implements Derivation {
    }

    private record OrRight2(Derivation right) implements Derivation {
    }

    /** Split the conjunction {@code principal}. */
    private record AndLeft(ConjunctionClassic principal, Derivation rest) implements Derivation {
    }

    /** Cases on the disjunction {@code principal}. */
    private record OrLeft(DisjunctionClassic principal, Derivation left, Derivation right) implements Derivation {
    }

    /** {@code principal} is {@code X -> B} and {@code X} is in the context: add {@code B}. */
    private record ArrowKnown(ClassicalLogicOperation principal, Derivation rest) implements Derivation {
    }

    /** {@code principal} is {@code (C & D) -> B}: add {@code derived}, {@code C -> (D -> B)}. */
    private record ArrowAnd(ClassicalLogicOperation principal, ClassicalLogicOperation derived,
                            Derivation rest) implements Derivation {
    }

    /** {@code principal} is {@code (C | D) -> B}: add {@code left}, {@code C -> B}, and {@code right}, {@code D -> B}. */
    private record ArrowOr(ClassicalLogicOperation principal, ClassicalLogicOperation left,
                           ClassicalLogicOperation right, Derivation rest) implements Derivation {
    }

    /**
     * {@code principal} is {@code (C -> D) -> B}: with {@code derived}, {@code D -> B}, prove {@code C -> D}
     * ({@code first}), then go on with {@code B} ({@code rest}).
     */
    private record ArrowArrow(ClassicalLogicOperation principal, ClassicalLogicOperation derived,
                              Derivation first, Derivation rest) implements Derivation {
    }

    private Derivation search(List<ClassicalLogicOperation> gamma, ClassicalLogicOperation goal) {
        checkNotInterrupted();
        var sequent = new Sequent(Set.copyOf(gamma), goal);
        if (unprovable.contains(sequent)) {
            return null;
        }
        Derivation derivation = searchRules(gamma, goal);
        if (derivation == null) {
            unprovable.add(sequent);
        }
        return derivation;
    }

    private Derivation searchRules(List<ClassicalLogicOperation> gamma, ClassicalLogicOperation goal) {
        if (gamma.contains(goal)) {
            return new Axiom();
        }
        if (gamma.contains(FALSE)) {
            return new ExFalso();
        }
        Arrow goalArrow = arrow(goal);
        if (goalArrow != null) {
            Derivation body = search(with(gamma, null, goalArrow.antecedent()), goalArrow.consequent());
            return body == null ? null : new ArrowRight(body);
        }
        if (goal instanceof ConjunctionClassic conjunction) {
            Derivation left = search(gamma, conjunction.getLeft());
            Derivation right = left == null ? null : search(gamma, conjunction.getRight());
            return right == null ? null : new AndRight(left, right);
        }
        for (ClassicalLogicOperation formula : gamma) {
            Invertible invertible = invertibleLeft(gamma, formula, goal);
            if (invertible.applies()) {
                return invertible.derivation();
            }
        }
        return searchNonInvertible(gamma, goal);
    }

    /**
     * The result of trying an invertible left rule: when it applies, its derivation (null when the sequent has no
     * proof, which is then final because the rule is invertible).
     */
    private record Invertible(boolean applies, Derivation derivation) {
        private static final Invertible NOT_APPLICABLE = new Invertible(false, null);

        private static Invertible of(Derivation derivation) {
            return new Invertible(true, derivation);
        }
    }

    private Invertible invertibleLeft(List<ClassicalLogicOperation> gamma, ClassicalLogicOperation formula, ClassicalLogicOperation goal) {
        if (formula instanceof ConjunctionClassic conjunction) {
            Derivation rest = search(with(gamma, formula, conjunction.getLeft(), conjunction.getRight()), goal);
            return Invertible.of(rest == null ? null : new AndLeft(conjunction, rest));
        }
        if (formula instanceof DisjunctionClassic disjunction) {
            Derivation left = search(with(gamma, formula, disjunction.getLeft()), goal);
            Derivation right = left == null ? null : search(with(gamma, formula, disjunction.getRight()), goal);
            return Invertible.of(right == null ? null : new OrLeft(disjunction, left, right));
        }
        Arrow arrow = arrow(formula);
        if (arrow == null) {
            return Invertible.NOT_APPLICABLE;
        }
        ClassicalLogicOperation antecedent = arrow.antecedent();
        ClassicalLogicOperation consequent = arrow.consequent();
        if (antecedent.equals(FALSE)) {
            // FALSE -> B says nothing
            return Invertible.of(search(with(gamma, formula), goal));
        }
        if (gamma.contains(antecedent)) {
            Derivation rest = search(with(gamma, formula, consequent), goal);
            return Invertible.of(rest == null ? null : new ArrowKnown(formula, rest));
        }
        if (antecedent instanceof ConjunctionClassic conjunction) {
            var derived = arrowOf(conjunction.getLeft(), arrowOf(conjunction.getRight(), consequent));
            Derivation rest = search(with(gamma, formula, derived), goal);
            return Invertible.of(rest == null ? null : new ArrowAnd(formula, derived, rest));
        }
        if (antecedent instanceof DisjunctionClassic disjunction) {
            var left = arrowOf(disjunction.getLeft(), consequent);
            var right = arrowOf(disjunction.getRight(), consequent);
            Derivation rest = search(with(gamma, formula, left, right), goal);
            return Invertible.of(rest == null ? null : new ArrowOr(formula, left, right, rest));
        }
        return Invertible.NOT_APPLICABLE;
    }

    private Derivation searchNonInvertible(List<ClassicalLogicOperation> gamma, ClassicalLogicOperation goal) {
        if (goal instanceof DisjunctionClassic disjunction) {
            Derivation left = search(gamma, disjunction.getLeft());
            if (left != null) {
                return new OrRight1(left);
            }
            Derivation right = search(gamma, disjunction.getRight());
            if (right != null) {
                return new OrRight2(right);
            }
        }
        for (ClassicalLogicOperation formula : gamma) {
            Arrow arrow = arrow(formula);
            Arrow inner = arrow == null ? null : arrow(arrow.antecedent());
            if (inner != null) {
                var derived = arrowOf(inner.consequent(), arrow.consequent());
                Derivation first = search(with(gamma, formula, derived), arrow.antecedent());
                if (first != null) {
                    Derivation rest = search(with(gamma, formula, arrow.consequent()), goal);
                    if (rest != null) {
                        return new ArrowArrow(formula, derived, first, rest);
                    }
                }
            }
        }
        return null;
    }

    private static Arrow arrow(ClassicalLogicOperation formula) {
        if (formula instanceof ImplicationClassic implication) {
            return new Arrow(implication.getLeft(), implication.getRight());
        }
        if (formula instanceof NegationClassic negation) {
            return new Arrow(negation.getElement(), FALSE);
        }
        return null;
    }

    /**
     * The formula {@code antecedent -> consequent} that the solver derives: {@code - antecedent} when the consequent is
     * {@code FALSE}.
     */
    private static ClassicalLogicOperation arrowOf(ClassicalLogicOperation antecedent, ClassicalLogicOperation consequent) {
        return consequent.equals(FALSE) ? new NegationClassic(antecedent) : new ImplicationClassic(antecedent, consequent);
    }

    private static List<ClassicalLogicOperation> distinct(List<ClassicalLogicOperation> formulas) {
        return with(List.of(), null, formulas.toArray(ClassicalLogicOperation[]::new));
    }

    /**
     * {@code gamma} without {@code removed} (when not null) and with {@code added}, keeping the order and without
     * repetitions.
     */
    private static List<ClassicalLogicOperation> with(List<ClassicalLogicOperation> gamma, ClassicalLogicOperation removed,
                                                      ClassicalLogicOperation... added) {
        var result = new ArrayList<>(gamma);
        if (removed != null) {
            result.remove(removed);
        }
        for (ClassicalLogicOperation formula : added) {
            if (!result.contains(formula)) {
                result.add(formula);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Translation into natural deduction
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Writes the steps that derive {@code goal} following {@code derivation}.
     *
     * @param context the line (1-based) of each formula of the sequent, all of them valid and in scope; it may gain
     *                the formulas the derivation adds to the context
     * @return the line of {@code goal}, in the current scope
     */
    private int derive(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation goal, Derivation derivation) {
        return switch (derivation) {
            case Axiom() -> context.get(goal);
            case ExFalso() -> apply(new ClassicFE(context.get(FALSE), goal));
            case ArrowRight(Derivation body) ->
                    hypothetical(context, goal, inner -> derive(inner, arrow(goal).consequent(), body));
            case AndRight(Derivation left, Derivation right) -> {
                var conjunction = (ConjunctionClassic) goal;
                int leftLine = derive(new HashMap<>(context), conjunction.getLeft(), left);
                int rightLine = derive(new HashMap<>(context), conjunction.getRight(), right);
                yield apply(new ClassicAndI(leftLine, rightLine));
            }
            case OrRight1(Derivation left) -> {
                var disjunction = (DisjunctionClassic) goal;
                yield apply(new ClassicOrI1(derive(context, disjunction.getLeft(), left), disjunction.getRight()));
            }
            case OrRight2(Derivation right) -> {
                var disjunction = (DisjunctionClassic) goal;
                yield apply(new ClassicOrI2(derive(context, disjunction.getRight(), right), disjunction.getLeft()));
            }
            case AndLeft(ConjunctionClassic principal, Derivation rest) -> {
                int line = context.get(principal);
                addIfAbsent(context, principal.getLeft(), () -> apply(new ClassicAndE1(line)));
                addIfAbsent(context, principal.getRight(), () -> apply(new ClassicAndE2(line)));
                yield derive(context, goal, rest);
            }
            case OrLeft(DisjunctionClassic principal, Derivation left, Derivation right) -> {
                int leftCase = hypothetical(context, new ImplicationClassic(principal.getLeft(), goal),
                        inner -> derive(inner, goal, left));
                int rightCase = hypothetical(context, new ImplicationClassic(principal.getRight(), goal),
                        inner -> derive(inner, goal, right));
                yield apply(new ClassicOrE(context.get(principal), leftCase, rightCase));
            }
            case ArrowKnown(ClassicalLogicOperation principal, Derivation rest) -> {
                var arrow = arrow(principal);
                addIfAbsent(context, arrow.consequent(),
                        () -> applyArrow(context.get(principal), context.get(arrow.antecedent())));
                yield derive(context, goal, rest);
            }
            case ArrowAnd(ClassicalLogicOperation principal, ClassicalLogicOperation derived, Derivation rest) -> {
                addIfAbsent(context, derived, () -> deriveArrowAnd(context, principal, derived));
                yield derive(context, goal, rest);
            }
            case ArrowOr(ClassicalLogicOperation principal, ClassicalLogicOperation left, ClassicalLogicOperation right,
                         Derivation rest) -> {
                var disjunction = (DisjunctionClassic) arrow(principal).antecedent();
                addIfAbsent(context, left, () -> hypothetical(context, left, inner ->
                        applyArrow(inner.get(principal), apply(new ClassicOrI1(inner.get(disjunction.getLeft()), disjunction.getRight())))));
                addIfAbsent(context, right, () -> hypothetical(context, right, inner ->
                        applyArrow(inner.get(principal), apply(new ClassicOrI2(inner.get(disjunction.getRight()), disjunction.getLeft())))));
                yield derive(context, goal, rest);
            }
            case ArrowArrow(ClassicalLogicOperation principal, ClassicalLogicOperation derived, Derivation first,
                            Derivation rest) -> {
                var arrow = arrow(principal);
                var antecedent = arrow.antecedent();
                var inner = arrow(antecedent);
                var withDerived = new HashMap<>(context);
                // D -> B: from D, the antecedent C -> D holds (assume C, repeat D), and so does B
                addIfAbsent(withDerived, derived, () -> hypothetical(context, derived, outer ->
                        applyArrow(outer.get(principal), hypothetical(outer, antecedent, deeper -> deeper.get(inner.consequent())))));
                int antecedentLine = derive(withDerived, antecedent, first);
                var withConsequent = new HashMap<>(context);
                withConsequent.put(arrow.consequent(), applyArrow(context.get(principal), antecedentLine));
                yield derive(withConsequent, goal, rest);
            }
        };
    }

    /**
     * {@code derived} is {@code C -> (D -> B)} for the {@code principal} {@code (C & D) -> B}: assume C, assume D,
     * {@code &I}, then {@code B}.
     */
    private int deriveArrowAnd(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation principal,
                               ClassicalLogicOperation derived) {
        var conjunction = (ConjunctionClassic) arrow(principal).antecedent();
        var innerArrow = arrow(derived).consequent();
        return hypothetical(context, derived, outer -> hypothetical(outer, innerArrow, inner ->
                applyArrow(inner.get(principal),
                        apply(new ClassicAndI(inner.get(conjunction.getLeft()), inner.get(conjunction.getRight()))))));
    }

    private static void addIfAbsent(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation formula,
                                    IntSupplier line) {
        if (!context.containsKey(formula)) {
            context.put(formula, line.getAsInt());
        }
    }

    /**
     * Proves the implication or negation {@code target} in a subproof: assumes its antecedent, runs {@code body} (which
     * gives the line of the consequent), repeats the consequent if it is not the last line, and discharges the
     * assumption with {@code ->I} or {@code -I}, as {@code target} is an implication or a negation.
     *
     * @return the line of {@code target}
     */
    private int hypothetical(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target,
                             ToIntFunction<Map<ClassicalLogicOperation, Integer>> body) {
        var antecedent = arrow(target).antecedent();
        var inner = new HashMap<>(context);
        inner.put(antecedent, apply(new ClassicAssume(antecedent)));
        int consequent = body.applyAsInt(inner);
        if (consequent != lastLine()) {
            apply(new ClassicCopy(consequent));
        }
        return apply(target instanceof NegationClassic ? new ClassicNotI() : new ClassicDeductionTheorem());
    }

    /**
     * Applies the implication or negation at line {@code arrowLine} to its antecedent at line {@code antecedentLine}:
     * {@code ->E} or {@code FI}.
     *
     * @return the line of the consequent
     */
    private int applyArrow(int arrowLine, int antecedentLine) {
        if (proof.getSteps().get(arrowLine - 1).getStep() instanceof NegationClassic) {
            return apply(new ClassicFI(antecedentLine, arrowLine));
        }
        return apply(new ClassicModusPonens(arrowLine, antecedentLine));
    }

    private int apply(ClassicalAction action) {
        if (!action.isValid(proof)) {
            throw new IllegalStateException("The intuitionistic solver built an invalid step: " + action.getAction());
        }
        action.apply(proof);
        return lastLine();
    }

    private int lastLine() {
        return proof.getSteps().size();
    }
}
