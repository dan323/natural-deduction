package com.dan323.proof.modal.internal;

import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;

import java.util.ArrayDeque;
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

/**
 * The automatic solver of modal logic.
 *
 * <p>The rules of modal logic describe S4: the relation {@code <=} between states is reflexive ({@code Refl}) and
 * transitive ({@code Trans}), {@code []I} and {@code <>E} introduce a fresh state. The solver first looks for a
 * closed tableau of the labelled sequent calculus for S4: it refutes the set {@code premises, - goal}, all of them in
 * the initial state, together with the relations among the premises. Its rules are those of the classical solver in
 * every state, plus: {@code [] A} in {@code s} and {@code s <= t} add {@code A} in {@code t} and, for the
 * transitivity, {@code [] A} in {@code t} (likewise {@code - (<> A)} adds {@code - A} and {@code - (<> A)});
 * {@code <> A} in {@code s} adds a new state {@code t} with {@code s <= t} and {@code A} in {@code t} (likewise
 * {@code - ([] A)} adds {@code - A}), unless some {@code t} with {@code s <= t} already has it. A new state whose
 * formulas are all in a state before it is blocked: its {@code <>} formulas are not expanded, as the earlier state
 * already accounts for them. With this loop check the calculus is complete for S4 and the search terminates in
 * principle, but it can take exponential time (in the number of branches and of states), so a large goal may run
 * into the caller's solve timeout. {@code FALSE} closes a branch; {@code TRUE} and the Next and Until formulas, which
 * modal logic has no rules for, are treated as atoms. The rule applications that the closed tableau does not need are
 * then pruned.
 *
 * <p>When there is a closed tableau it is translated into natural deduction steps, as the classical solver does:
 * introduction rules for the goal ({@code ->I}, {@code -I}, {@code &I}, {@code |I1/2}, {@code []I}, {@code <>I})
 * while the rest of the tableau does not need {@code - goal}, and otherwise {@code - goal} assumed, {@code FALSE}
 * derived, {@code -I} and {@code -E}. A new state becomes the fresh state of a {@code []I} or {@code <>E}, and the
 * relations come from the premises, those assumptions, {@code Refl} and {@code Trans}. When there is no closed tableau
 * the proof is left with its premises.
 *
 * @author daniel
 */
public final class ModalAutomate {

    private static final ModalLogicalOperation FALSE = ConstantModal.FALSE;

    private final Map<Tableau, Set<Labelled>> references = new IdentityHashMap<>();
    private ModalNaturalDeduction proof;
    private int newStates;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public ModalAutomate() {
        // Nothing to set up: automate initializes the state
    }

    /**
     * Restarts the proof from its premises and finishes it, with the goal in the initial state, if the goal follows
     * from them. Otherwise the proof is left with its premises only.
     *
     * <p>It checks the interrupt flag of the calling thread during the search: interrupting the thread stops it with a
     * {@link CancellationException}.
     *
     * @param naturalDeduction the proof to solve
     * @throws CancellationException if the calling thread is interrupted
     */
    public void automate(ModalNaturalDeduction naturalDeduction) {
        proof = naturalDeduction;
        proof.reset();
        references.clear();
        newStates = 0;
        checkNotInterrupted();
        var context = new Context();
        var branch = new Branch();
        branch.worlds.add(proof.getState0());
        for (int i = 0; i < proof.getSteps().size(); i++) {
            ProofStepModal step = proof.getSteps().get(i);
            if (step.getStep() instanceof ModalLogicalOperation formula) {
                var labelled = new Labelled(step.getState(), formula);
                branch.add(labelled);
                context.lines.putIfAbsent(labelled, i + 1);
            } else if (step.getStep() instanceof LessEqual relation) {
                var edge = new Edge(relation.getLeft(), relation.getRight());
                branch.edges.add(edge);
                addWorld(branch, relation.getLeft());
                addWorld(branch, relation.getRight());
                context.relations.putIfAbsent(edge, i + 1);
            }
        }
        if (proof.getGoal() instanceof LessEqual relation) {
            deriveRelation(context, relation);
            return;
        }
        if (!(proof.getGoal() instanceof ModalLogicalOperation goal)) {
            return;
        }
        var target = new Labelled(proof.getState0(), goal);
        if (!goal.equals(FALSE)) {
            branch.add(negate(target));
        }
        Tableau tableau = search(branch);
        if (tableau == null) {
            return;
        }
        int line = derive(context, target, prune(tableau));
        if (line != lastLine()) {
            apply(new ModalCopy(line));
        }
    }

    private static void checkNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("The automatic solver was interrupted");
        }
    }

    private static void addWorld(Branch branch, String world) {
        if (!branch.worlds.contains(world)) {
            branch.worlds.add(world);
        }
    }

    /**
     * A relation goal {@code s <= t} follows from the relations among the premises ({@code Trans}), or from
     * {@code Refl} when {@code s} is {@code t} and some premise is in {@code s}.
     */
    private void deriveRelation(Context context, LessEqual goal) {
        var edge = new Edge(goal.getLeft(), goal.getRight());
        int reflexive = context.lines.entrySet().stream()
                .filter(entry -> entry.getKey().world().equals(goal.getLeft()))
                .mapToInt(Map.Entry::getValue)
                .findFirst().orElse(-1);
        boolean byReflexivity = goal.getLeft().equals(goal.getRight()) && reflexive > 0;
        if (context.relations.containsKey(edge) || byReflexivity || findPath(context, edge) != null) {
            relation(context, goal.getLeft(), goal.getRight(), reflexive);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Search: a labelled tableau for S4
    // ------------------------------------------------------------------------------------------------------------

    /**
     * A formula in a state of the search. The states the search creates have names ({@code #1}, {@code #2}, ...) that
     * the translation replaces by fresh states of the proof.
     */
    private record Labelled(String world, ModalLogicalOperation formula) {
    }

    /** The relation {@code from <= to}. */
    private record Edge(String from, String to) {
    }

    /**
     * A branch of the tableau: its formulas, those already expanded, its relations and its states.
     */
    private static final class Branch {
        private final List<Labelled> formulas = new ArrayList<>();
        private final Set<Labelled> contents = new HashSet<>();
        private final Set<Labelled> expanded = new HashSet<>();
        private final List<Edge> edges = new ArrayList<>();
        private final List<String> worlds = new ArrayList<>();
        private final Set<String> created = new HashSet<>();

        private void add(Labelled formula) {
            if (contents.add(formula)) {
                formulas.add(formula);
            }
        }

        private boolean contains(Labelled formula) {
            return contents.contains(formula);
        }

        private Branch copy() {
            var copy = new Branch();
            copy.formulas.addAll(formulas);
            copy.contents.addAll(contents);
            copy.expanded.addAll(expanded);
            copy.edges.addAll(edges);
            copy.worlds.addAll(worlds);
            copy.created.addAll(created);
            return copy;
        }

        /** Whether {@code from <= to}: the reflexive and transitive closure of the edges. */
        private boolean reaches(String from, String to) {
            if (from.equals(to)) {
                return true;
            }
            var seen = new HashSet<String>();
            var pending = new ArrayDeque<String>();
            pending.add(from);
            while (!pending.isEmpty()) {
                String world = pending.poll();
                for (Edge edge : edges) {
                    if (edge.from().equals(world) && seen.add(edge.to())) {
                        if (edge.to().equals(to)) {
                            return true;
                        }
                        pending.add(edge.to());
                    }
                }
            }
            return false;
        }

        private Set<ModalLogicalOperation> formulasAt(String world) {
            var result = new HashSet<ModalLogicalOperation>();
            formulas.stream().filter(formula -> formula.world().equals(world)).forEach(formula -> result.add(formula.formula()));
            return result;
        }

        /**
         * A state that the search created is blocked when a state before it has all its formulas.
         */
        private boolean blocked(String world) {
            if (!created.contains(world)) {
                return false;
            }
            Set<ModalLogicalOperation> own = formulasAt(world);
            return worlds.stream().anyMatch(other -> !other.equals(world) && reaches(other, world) && formulasAt(other).containsAll(own));
        }
    }

    /**
     * A closed tableau: each record is the rule applied to the formulas of its branch, with the tableaux of the
     * branches it leads to.
     */
    private sealed interface Tableau {
    }

    /** {@code formula} and its negation are in the branch. */
    private record Closed(Labelled formula) implements Tableau {
    }

    /** {@code FALSE} is in the branch, in {@code world}. */
    private record Falsum(String world) implements Tableau {
    }

    /** {@code principal} adds both its {@link #alpha components}, in its state. */
    private record Alpha(Labelled principal, Tableau rest) implements Tableau {
    }

    /** {@code principal} adds one of its {@link #beta components} in each branch, in its state. */
    private record Beta(Labelled principal, Tableau left, Tableau right) implements Tableau {
    }

    /** {@code principal}, {@code [] A} or {@code - (<> A)}, adds {@code A} or {@code - A} in {@code world}. */
    private record Necessary(Labelled principal, String world, Tableau rest) implements Tableau {
    }

    /** {@code principal}, {@code [] A} or {@code - (<> A)}, adds itself in {@code world}. */
    private record Transitive4(Labelled principal, String world, Tableau rest) implements Tableau {
    }

    /**
     * {@code principal}, {@code <> A} or {@code - ([] A)}, adds the new state {@code world}, after its state, with
     * {@code A} or {@code - A}.
     */
    private record Possible(Labelled principal, String world, Tableau rest) implements Tableau {
    }

    private Tableau search(Branch branch) {
        checkNotInterrupted();
        for (Labelled labelled : branch.formulas) {
            if (labelled.formula().equals(FALSE)) {
                return new Falsum(labelled.world());
            }
        }
        for (Labelled labelled : branch.formulas) {
            if (labelled.formula() instanceof NegationModal negation
                    && branch.contains(new Labelled(labelled.world(), negation.getElement()))) {
                return new Closed(new Labelled(labelled.world(), negation.getElement()));
            }
        }
        Labelled principal = null;
        for (Labelled labelled : branch.formulas) {
            if (!branch.expanded.contains(labelled) && priority(labelled.formula()) < priority(principal == null ? null : principal.formula())) {
                principal = labelled;
            }
        }
        int priority = priority(principal == null ? null : principal.formula());
        if (priority <= 2) {
            return expand(branch, principal);
        }
        Tableau necessary = searchNecessary(branch);
        if (necessary != null) {
            return necessary == OPEN ? null : necessary;
        }
        if (priority == 3) {
            return expand(branch, principal);
        }
        return searchPossible(branch);
    }

    /** Marks a search that found an open branch, where a rule was still applied. */
    private static final Tableau OPEN = new Falsum("");

    /**
     * The order in which the propositional rules are applied, lowest first; the rules of {@code []} come between 2
     * and 3, and those of {@code <>} last. Every rule is invertible, so it only shapes the proof: cases on a
     * disjunction come before the rule of {@code - (A | B)}, which the translation can seldom turn into an
     * introduction rule. {@link Integer#MAX_VALUE} for a formula without a propositional rule, and for no formula.
     */
    private static int priority(ModalLogicalOperation formula) {
        if (formula == null) {
            return Integer.MAX_VALUE;
        }
        if (alpha(formula) != null) {
            boolean negatedDisjunction = formula instanceof NegationModal negation && negation.getElement() instanceof DisjunctionModal;
            return negatedDisjunction ? 2 : 0;
        }
        if (beta(formula) != null) {
            return formula instanceof DisjunctionModal ? 1 : 3;
        }
        return Integer.MAX_VALUE;
    }

    private Tableau expand(Branch branch, Labelled principal) {
        String world = principal.world();
        List<ModalLogicalOperation> alpha = alpha(principal.formula());
        if (alpha != null) {
            var next = branch.copy();
            next.expanded.add(principal);
            alpha.forEach(component -> next.add(new Labelled(world, component)));
            Tableau rest = search(next);
            return rest == null ? null : new Alpha(principal, rest);
        }
        List<ModalLogicalOperation> beta = beta(principal.formula());
        var left = branch.copy();
        left.expanded.add(principal);
        left.add(new Labelled(world, beta.get(0)));
        Tableau leftTableau = search(left);
        if (leftTableau == null) {
            return null;
        }
        var right = branch.copy();
        right.expanded.add(principal);
        right.add(new Labelled(world, beta.get(1)));
        Tableau rightTableau = search(right);
        return rightTableau == null ? null : new Beta(principal, leftTableau, rightTableau);
    }

    /**
     * Applies the first rule of {@code []} or {@code - <>} that adds a formula.
     *
     * @return null when none does, {@link #OPEN} when one does and the branch stays open
     */
    private Tableau searchNecessary(Branch branch) {
        for (Labelled labelled : List.copyOf(branch.formulas)) {
            ModalLogicalOperation component = necessary(labelled.formula());
            if (component != null) {
                for (String world : branch.worlds) {
                    if (branch.reaches(labelled.world(), world) && !branch.contains(new Labelled(world, component))) {
                        var next = branch.copy();
                        next.add(new Labelled(world, component));
                        Tableau rest = search(next);
                        return rest == null ? OPEN : new Necessary(labelled, world, rest);
                    }
                }
            }
        }
        for (Labelled labelled : List.copyOf(branch.formulas)) {
            if (necessary(labelled.formula()) != null) {
                for (String world : branch.worlds) {
                    if (branch.reaches(labelled.world(), world) && !branch.contains(new Labelled(world, labelled.formula()))) {
                        var next = branch.copy();
                        next.add(new Labelled(world, labelled.formula()));
                        Tableau rest = search(next);
                        return rest == null ? OPEN : new Transitive4(labelled, world, rest);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Applies the first rule of {@code <>} or {@code - []} in a state that is not blocked; a formula that a later
     * state already has is just marked as expanded.
     */
    private Tableau searchPossible(Branch branch) {
        for (Labelled labelled : branch.formulas) {
            ModalLogicalOperation component = possible(labelled.formula());
            if (component != null && !branch.expanded.contains(labelled) && !branch.blocked(labelled.world())) {
                var next = branch.copy();
                next.expanded.add(labelled);
                boolean witnessed = branch.worlds.stream().anyMatch(world ->
                        branch.reaches(labelled.world(), world) && branch.contains(new Labelled(world, component)));
                if (witnessed) {
                    return search(next);
                }
                String world = "#" + (++newStates);
                next.worlds.add(world);
                next.created.add(world);
                next.edges.add(new Edge(labelled.world(), world));
                next.add(new Labelled(world, component));
                Tableau rest = search(next);
                return rest == null ? null : new Possible(labelled, world, rest);
            }
        }
        return null;
    }

    /**
     * The two formulas that {@code formula} adds together: {@code A & B} gives {@code A} and {@code B};
     * {@code - (A | B)} gives {@code - A} and {@code - B}; {@code - (A -> B)} gives {@code A} and {@code - B};
     * {@code - (- A)} gives {@code A} (twice). Null when {@code formula} is not of these kinds.
     */
    private static List<ModalLogicalOperation> alpha(ModalLogicalOperation formula) {
        if (formula instanceof ConjunctionModal conjunction) {
            return List.of(conjunction.getLeft(), conjunction.getRight());
        }
        if (formula instanceof NegationModal negation) {
            if (negation.getElement() instanceof DisjunctionModal disjunction) {
                return List.of(negate(disjunction.getLeft()), negate(disjunction.getRight()));
            }
            if (negation.getElement() instanceof ImplicationModal implication) {
                return List.of(implication.getLeft(), negate(implication.getRight()));
            }
            if (negation.getElement() instanceof NegationModal inner) {
                return List.of(inner.getElement(), inner.getElement());
            }
        }
        return null;
    }

    /**
     * The two formulas, one for each branch, that {@code formula} adds: {@code A | B} gives {@code A} and {@code B};
     * {@code A -> B} gives {@code - A} and {@code B}; {@code - (A & B)} gives {@code - A} and {@code - B}. Null when
     * {@code formula} is not of these kinds.
     */
    private static List<ModalLogicalOperation> beta(ModalLogicalOperation formula) {
        if (formula instanceof DisjunctionModal disjunction) {
            return List.of(disjunction.getLeft(), disjunction.getRight());
        }
        if (formula instanceof ImplicationModal implication) {
            return List.of(negate(implication.getLeft()), implication.getRight());
        }
        if (formula instanceof NegationModal negation && negation.getElement() instanceof ConjunctionModal conjunction) {
            return List.of(negate(conjunction.getLeft()), negate(conjunction.getRight()));
        }
        return null;
    }

    /** What {@code [] A} ({@code A}) or {@code - (<> A)} ({@code - A}) adds in every later state; null otherwise. */
    private static ModalLogicalOperation necessary(ModalLogicalOperation formula) {
        if (formula instanceof Always always) {
            return always.getElement();
        }
        if (formula instanceof NegationModal negation && negation.getElement() instanceof Sometime sometime) {
            return negate(sometime.getElement());
        }
        return null;
    }

    /** What {@code <> A} ({@code A}) or {@code - ([] A)} ({@code - A}) adds in a new state; null otherwise. */
    private static ModalLogicalOperation possible(ModalLogicalOperation formula) {
        if (formula instanceof Sometime sometime) {
            return sometime.getElement();
        }
        if (formula instanceof NegationModal negation && negation.getElement() instanceof Always always) {
            return negate(always.getElement());
        }
        return null;
    }

    private static NegationModal negate(ModalLogicalOperation formula) {
        return new NegationModal(formula);
    }

    private static Labelled negate(Labelled labelled) {
        return new Labelled(labelled.world(), negate(labelled.formula()));
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
            case Closed(Labelled formula) -> referencing(tableau, Set.of(formula, negate(formula)));
            case Falsum(String world) -> referencing(tableau, Set.of(new Labelled(world, FALSE)));
            case Alpha(Labelled principal, Tableau rest) -> {
                Tableau pruned = prune(rest);
                if (alpha(principal.formula()).stream().noneMatch(component -> mentions(pruned, new Labelled(principal.world(), component)))) {
                    yield pruned;
                }
                yield referencing(new Alpha(principal, pruned), references(pruned), Set.of(principal));
            }
            case Beta(Labelled principal, Tableau left, Tableau right) -> {
                List<ModalLogicalOperation> beta = beta(principal.formula());
                Tableau leftPruned = prune(left);
                if (!mentions(leftPruned, new Labelled(principal.world(), beta.get(0)))) {
                    yield leftPruned;
                }
                Tableau rightPruned = prune(right);
                if (!mentions(rightPruned, new Labelled(principal.world(), beta.get(1)))) {
                    yield rightPruned;
                }
                yield referencing(new Beta(principal, leftPruned, rightPruned), references(leftPruned),
                        references(rightPruned), Set.of(principal));
            }
            case Necessary(Labelled principal, String world, Tableau rest) -> {
                Tableau pruned = prune(rest);
                if (!mentions(pruned, new Labelled(world, necessary(principal.formula())))) {
                    yield pruned;
                }
                yield referencing(new Necessary(principal, world, pruned), references(pruned), Set.of(principal));
            }
            case Transitive4(Labelled principal, String world, Tableau rest) -> {
                Tableau pruned = prune(rest);
                if (!mentions(pruned, new Labelled(world, principal.formula()))) {
                    yield pruned;
                }
                yield referencing(new Transitive4(principal, world, pruned), references(pruned), Set.of(principal));
            }
            case Possible(Labelled principal, String world, Tableau rest) -> {
                Tableau pruned = prune(rest);
                if (references(pruned).stream().noneMatch(formula -> formula.world().equals(world))) {
                    yield pruned;
                }
                yield referencing(new Possible(principal, world, pruned), references(pruned), Set.of(principal));
            }
        };
    }

    @SafeVarargs
    private Tableau referencing(Tableau tableau, Set<Labelled>... formulas) {
        var all = new HashSet<Labelled>();
        for (Set<Labelled> set : formulas) {
            all.addAll(set);
        }
        references.put(tableau, all);
        return tableau;
    }

    private Set<Labelled> references(Tableau tableau) {
        return references.get(tableau);
    }

    private boolean mentions(Tableau tableau, Labelled formula) {
        return references(tableau).contains(formula);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Translation into natural deduction
    // ------------------------------------------------------------------------------------------------------------

    /**
     * What the translation knows in the current scope: the line (1-based) of each formula and relation of the branch,
     * all of them valid and in scope, and the proof state of each state the search created.
     */
    private static final class Context {
        private final Map<Labelled, Integer> lines = new HashMap<>();
        private final Map<Edge, Integer> relations = new HashMap<>();
        private final Map<String, String> states = new HashMap<>();

        private Context copy() {
            var copy = new Context();
            copy.lines.putAll(lines);
            copy.relations.putAll(relations);
            copy.states.putAll(states);
            return copy;
        }

        private String state(String world) {
            return states.getOrDefault(world, world);
        }
    }

    private int line(Context context, Labelled formula) {
        Integer line = context.lines.get(formula);
        if (line == null) {
            throw new IllegalStateException("The modal solver lost the line of " + formula);
        }
        return line;
    }

    /**
     * Writes the steps that derive {@code target} following {@code tableau}.
     *
     * <p>When {@code target} is {@code FALSE} (in any state), every formula of the branch is in {@code context}, and
     * the line returned is {@code FALSE} in the initial state. Otherwise the branch also has the negation of
     * {@code target}, which has no line: the translation goes for {@code target} itself, and assumes its negation only
     * when the tableau needs it.
     *
     * @param context the lines of the branch; it may gain the formulas the tableau adds to the branch
     * @return the line of {@code target}, in the current scope
     */
    private int derive(Context context, Labelled target, Tableau tableau) {
        boolean refutation = target.formula().equals(FALSE);
        if (!refutation && context.lines.containsKey(target)) {
            return context.lines.get(target);
        }
        Labelled negated = refutation ? null : negate(target);
        if (negated != null && context.lines.containsKey(negated)) {
            return fromFalse(context, derive(context, falsum(), tableau), target);
        }
        if (negated != null && context.lines.containsKey(negate(negated))) {
            return apply(new ModalNotE(context.lines.get(negate(negated))));
        }
        if (negated != null && needsNegatedTarget(tableau, target, negated)) {
            return byContradiction(context, target, inner -> derive(inner, falsum(), tableau));
        }
        return switch (tableau) {
            case Falsum(String world) -> fromFalse(context, inInitialState(line(context, new Labelled(world, FALSE))), target);
            case Closed(Labelled formula) ->
                    fromFalse(context, apply(new ModalFI(line(context, formula), line(context, negate(formula)))), target);
            case Alpha(Labelled principal, Tableau rest) when principal.equals(negated) -> introduce(context, target, rest);
            case Alpha(Labelled principal, Tableau rest) -> {
                addAlpha(context, principal, references(rest));
                yield derive(context, target, rest);
            }
            case Beta(Labelled principal, Tableau left, Tableau right) when principal.equals(negated) -> {
                // - (A & B) for the target A & B
                var conjunction = (ConjunctionModal) target.formula();
                int leftLine = derive(context.copy(), new Labelled(target.world(), conjunction.getLeft()), left);
                int rightLine = derive(context.copy(), new Labelled(target.world(), conjunction.getRight()), right);
                yield apply(new ModalAndI(leftLine, rightLine));
            }
            case Beta(Labelled principal, Tableau left, Tableau right) -> beta(context, target, principal, left, right);
            case Necessary(Labelled principal, String world, Tableau rest) when principal.equals(negated) -> {
                // - (<> A) for the target <> A: A in a later state gives it
                var element = ((Sometime) target.formula()).getElement();
                int holds = derive(context, new Labelled(world, element), rest);
                yield apply(new ModalDiaI(holds, relation(context, target.world(), world, holds)));
            }
            case Necessary(Labelled principal, String world, Tableau rest) -> {
                addNecessary(context, principal, world, references(rest));
                yield derive(context, target, rest);
            }
            case Transitive4(Labelled principal, String world, Tableau rest) -> {
                addTransitive(context, principal, world, references(rest));
                yield derive(context, target, rest);
            }
            case Possible(Labelled principal, String world, Tableau rest) when principal.equals(negated) ->
                    // - ([] A) for the target [] A
                    necessitate(context, target.world(), ((Always) target.formula()).getElement(), world, rest);
            case Possible(Labelled principal, String world, Tableau rest) when principal.formula() instanceof Sometime sometime ->
                    possibly(context, target, principal, sometime.getElement(), world, rest);
            case Possible(Labelled principal, String world, Tableau rest) -> {
                // - ([] A): [] A contradicts it
                var always = (Always) ((NegationModal) principal.formula()).getElement();
                int box = necessitate(context, principal.world(), always.getElement(), world, rest);
                yield fromFalse(context, apply(new ModalFI(box, line(context, principal))), target);
            }
        };
    }

    private Labelled falsum() {
        return new Labelled(proof.getState0(), FALSE);
    }

    /**
     * Whether translating the node {@code tableau} for {@code target} needs the line of {@code negated}, its negation:
     * when it closes with it or applies a rule of {@code - <>} to it, when it goes on with another target (whose
     * translation cannot use {@code negated}) and the tableau there uses it, and for cases on a disjunction in another
     * state ({@code |E} needs the state of the disjunction).
     */
    private boolean needsNegatedTarget(Tableau tableau, Labelled target, Labelled negated) {
        return switch (tableau) {
            case Falsum falsum -> false;
            case Closed(Labelled formula) -> formula.equals(negated);
            case Alpha(Labelled principal, Tableau rest) when principal.equals(negated) -> {
                List<ModalLogicalOperation> components = alpha(principal.formula());
                // - (A | B) goes on with the target A or B, so the rest must not use the other one
                boolean both = target.formula() instanceof DisjunctionModal
                        && !components.get(0).equals(components.get(1))
                        && mentions(rest, new Labelled(target.world(), components.get(0)))
                        && mentions(rest, new Labelled(target.world(), components.get(1)));
                yield both || mentions(rest, negated);
            }
            case Alpha alpha -> false;
            case Beta(Labelled principal, Tableau left, Tableau right) when principal.formula() instanceof DisjunctionModal ->
                    !principal.world().equals(target.world());
            case Beta(Labelled principal, Tableau left, Tableau right) when principal.formula() instanceof ImplicationModal ->
                    mentions(left, negated);
            case Beta(Labelled principal, Tableau left, Tableau right) ->
                    mentions(left, negated) || mentions(right, negated);
            case Necessary(Labelled principal, String world, Tableau rest) ->
                    principal.equals(negated) && mentions(rest, negated);
            case Transitive4(Labelled principal, String world, Tableau rest) -> principal.equals(negated);
            case Possible(Labelled principal, String world, Tableau rest) when principal.formula() instanceof Sometime -> false;
            case Possible(Labelled principal, String world, Tableau rest) -> mentions(rest, negated);
        };
    }

    /**
     * The rule of the tableau applied to the negation of {@code target}, turned into an introduction rule for
     * {@code target}.
     */
    private int introduce(Context context, Labelled target, Tableau rest) {
        String world = target.world();
        return switch (target.formula()) {
            case ImplicationModal implication ->
                    hypothetical(context, target, inner -> derive(inner, new Labelled(world, implication.getRight()), rest));
            case NegationModal negation -> hypothetical(context, target, inner -> derive(inner, falsum(), rest));
            case DisjunctionModal disjunction when mentions(rest, new Labelled(world, negate(disjunction.getLeft()))) ->
                    apply(new ModalOrI1(derive(context, new Labelled(world, disjunction.getLeft()), rest), disjunction.getRight()));
            case DisjunctionModal disjunction ->
                    apply(new ModalOrI2(derive(context, new Labelled(world, disjunction.getRight()), rest), disjunction.getLeft()));
            default -> throw new IllegalStateException("No introduction rule for " + target);
        };
    }

    /**
     * Adds to {@code context} the components of {@code principal} (a formula with an {@link #alpha} rule) that are in
     * {@code used}.
     */
    private void addAlpha(Context context, Labelled principal, Set<Labelled> used) {
        int line = line(context, principal);
        String world = principal.world();
        List<ModalLogicalOperation> components = alpha(principal.formula());
        var first = new Labelled(world, components.get(0));
        var second = new Labelled(world, components.get(1));
        if (principal.formula() instanceof ConjunctionModal) {
            addIfUsed(context, used, first, () -> apply(new ModalAndE1(line)));
            addIfUsed(context, used, second, () -> apply(new ModalAndE2(line)));
            return;
        }
        switch (((NegationModal) principal.formula()).getElement()) {
            case DisjunctionModal disjunction -> {
                // - A: A gives A | B, which contradicts - (A | B); likewise - B
                addIfUsed(context, used, first, () -> hypothetical(context, first, inner -> apply(new ModalFI(
                        apply(new ModalOrI1(line(inner, new Labelled(world, disjunction.getLeft())), disjunction.getRight())), line))));
                addIfUsed(context, used, second, () -> hypothetical(context, second, inner -> apply(new ModalFI(
                        apply(new ModalOrI2(line(inner, new Labelled(world, disjunction.getRight())), disjunction.getLeft())), line))));
            }
            case ImplicationModal implication -> {
                var implies = new Labelled(world, implication);
                var consequent = implication.getRight();
                // A: with - A, A -> B holds (A gives FALSE), which contradicts - (A -> B)
                addIfUsed(context, used, first, () -> byContradiction(context, first, inner -> apply(new ModalFI(
                        hypothetical(inner, implies, deeper -> apply(new ModalFE(apply(new ModalFI(line(deeper, first),
                                line(deeper, negate(first)))), consequent, deeper.state(world)))),
                        line))));
                // - B: B gives A -> B, which contradicts - (A -> B)
                addIfUsed(context, used, second, () -> hypothetical(context, second, inner -> apply(new ModalFI(
                        hypothetical(inner, implies, deeper -> line(deeper, new Labelled(world, consequent))), line))));
            }
            default -> addIfUsed(context, used, first, () -> apply(new ModalNotE(line)));
        }
    }

    private static void addIfUsed(Context context, Set<Labelled> used, Labelled formula, IntSupplier line) {
        if (used.contains(formula) && !context.lines.containsKey(formula)) {
            context.lines.put(formula, line.getAsInt());
        }
    }

    /**
     * The rule of the tableau applied to {@code principal}, a formula with a {@link #beta} rule that is not the
     * negation of {@code target}.
     */
    private int beta(Context context, Labelled target, Labelled principal, Tableau left, Tableau right) {
        int line = line(context, principal);
        String world = principal.world();
        return switch (principal.formula()) {
            case DisjunctionModal disjunction -> {
                // In the state of the disjunction: target is FALSE or in that state
                ModalLogicalOperation goal = target.formula();
                int leftCase = hypothetical(context, new Labelled(world, new ImplicationModal(disjunction.getLeft(), goal)),
                        inner -> derive(inner, target, left));
                int rightCase = hypothetical(context, new Labelled(world, new ImplicationModal(disjunction.getRight(), goal)),
                        inner -> derive(inner, target, right));
                int cases = apply(new ModalOrE(line, leftCase, rightCase));
                yield goal.equals(FALSE) ? inInitialState(cases) : cases;
            }
            case ImplicationModal implication -> {
                int antecedent = derive(context.copy(), new Labelled(world, implication.getLeft()), left);
                context.lines.put(new Labelled(world, implication.getRight()), apply(new ModalModusPonens(line, antecedent)));
                yield derive(context, target, right);
            }
            default -> {
                // - (A & B): A & B contradicts it
                var conjunction = (ConjunctionModal) ((NegationModal) principal.formula()).getElement();
                int leftLine = derive(context.copy(), new Labelled(world, conjunction.getLeft()), left);
                int rightLine = derive(context.copy(), new Labelled(world, conjunction.getRight()), right);
                yield fromFalse(context, apply(new ModalFI(apply(new ModalAndI(leftLine, rightLine)), line)), target);
            }
        };
    }

    /**
     * Adds to {@code context}, when it is in {@code used}, what {@code principal} ({@code [] A} or {@code - (<> A)})
     * gives in {@code world}: {@code []E}, or {@code - A} because {@code A} would give {@code <> A} ({@code <>I}).
     */
    private void addNecessary(Context context, Labelled principal, String world, Set<Labelled> used) {
        var component = new Labelled(world, necessary(principal.formula()));
        if (!used.contains(component) || context.lines.containsKey(component)) {
            return;
        }
        int line = line(context, principal);
        if (principal.formula() instanceof Always) {
            int relation = relation(context, principal.world(), world, line);
            context.lines.put(component, apply(new ModalBoxE(line, relation)));
        } else {
            var element = ((NegationModal) component.formula()).getElement();
            context.lines.put(component, hypothetical(context, component, inner -> {
                int relation = relation(inner, principal.world(), world, line);
                return apply(new ModalFI(apply(new ModalDiaI(line(inner, new Labelled(world, element)), relation)), line));
            }));
        }
    }

    /**
     * Adds to {@code context}, when it is in {@code used}, {@code principal} ({@code [] A} or {@code - (<> A)}) in
     * {@code world}, a later state: for {@code [] A}, {@code A} holds in every state after {@code world}
     * ({@code Trans}, {@code []E}, {@code []I}); for {@code - (<> A)}, {@code <> A} in {@code world} would give
     * {@code <> A} in the state of the principal ({@code <>E}, {@code Trans}, {@code <>I}).
     */
    private void addTransitive(Context context, Labelled principal, String world, Set<Labelled> used) {
        var copy = new Labelled(world, principal.formula());
        if (!used.contains(copy) || context.lines.containsKey(copy)) {
            return;
        }
        int line = line(context, principal);
        int relation = relation(context, principal.world(), world, line);
        String state = context.state(world);
        if (principal.formula() instanceof Always always) {
            String later = freshState();
            int step = apply(new ModalAssume(new LessEqual(state, later)));
            apply(new Transitive(relation, step));
            apply(new ModalBoxE(line, lastLine()));
            context.lines.put(copy, apply(new ModalBoxI()));
            return;
        }
        var element = ((Sometime) ((NegationModal) principal.formula()).getElement()).getElement();
        context.lines.put(copy, hypothetical(context, copy, inner -> {
            int possible = lastLine();
            String later = freshState();
            int step = apply(new ModalAssume(new LessEqual(state, later)));
            int holds = apply(new ModalAssume(element, later));
            int transitive = apply(new Transitive(relation, step));
            apply(new ModalFI(apply(new ModalDiaI(holds, transitive)), line));
            return apply(new ModalDiaE(possible));
        }));
    }

    /**
     * Proves {@code [] element} in {@code world}: assumes {@code world <= s} for a fresh {@code s}, the state of
     * {@code later}, derives {@code element} there following {@code rest}, and ends with {@code []I}.
     *
     * @return the line of {@code [] element}
     */
    private int necessitate(Context context, String world, ModalLogicalOperation element, String later, Tableau rest) {
        var inner = context.copy();
        String state = freshState();
        inner.states.put(later, state);
        inner.relations.put(new Edge(world, later), apply(new ModalAssume(new LessEqual(context.state(world), state))));
        int line = derive(inner, new Labelled(later, element), rest);
        if (element.equals(FALSE)) {
            line = apply(new ModalFE(line, FALSE, state));
        } else if (line != lastLine()) {
            apply(new ModalCopy(line));
        }
        return apply(new ModalBoxI());
    }

    /**
     * Uses {@code principal}, {@code <> element}: assumes {@code s <= t} for a fresh {@code t}, the state of
     * {@code later}, and {@code element} in {@code t}, derives {@code target} following {@code rest}, and ends with
     * {@code <>E}.
     *
     * @return the line of {@code target}
     */
    private int possibly(Context context, Labelled target, Labelled principal, ModalLogicalOperation element,
                         String later, Tableau rest) {
        var inner = context.copy();
        String state = freshState();
        inner.states.put(later, state);
        inner.relations.put(new Edge(principal.world(), later),
                apply(new ModalAssume(new LessEqual(context.state(principal.world()), state))));
        inner.lines.put(new Labelled(later, element), apply(new ModalAssume(element, state)));
        int line = derive(inner, target, rest);
        if (line != lastLine()) {
            apply(new ModalCopy(line));
        }
        return apply(new ModalDiaE(line(context, principal)));
    }

    /**
     * The line of the relation {@code from <= to}: a relation of the branch, {@code Refl} (from the formula in
     * {@code from} at line {@code reflexive}) or a chain of {@code Trans}.
     */
    private int relation(Context context, String from, String to, int reflexive) {
        var edge = new Edge(from, to);
        Integer known = context.relations.get(edge);
        if (known != null) {
            return known;
        }
        int line;
        List<Edge> path = findPath(context, edge);
        if (from.equals(to) && reflexive > 0) {
            line = apply(new Reflexive(reflexive));
        } else if (path != null) {
            line = context.relations.get(path.get(0));
            for (int i = 1; i < path.size(); i++) {
                line = apply(new Transitive(line, context.relations.get(path.get(i))));
            }
        } else {
            throw new IllegalStateException("The modal solver found no relation " + from + " <= " + to);
        }
        context.relations.put(edge, line);
        return line;
    }

    /**
     * A chain of relations of {@code context} from {@code edge.from()} to a different {@code edge.to()}, or null.
     */
    private static List<Edge> findPath(Context context, Edge edge) {
        Map<String, Edge> reachedBy = new HashMap<>();
        var pending = new ArrayDeque<String>();
        pending.add(edge.from());
        while (!pending.isEmpty() && !reachedBy.containsKey(edge.to())) {
            String world = pending.poll();
            for (Edge next : context.relations.keySet()) {
                if (next.from().equals(world) && !next.to().equals(edge.from()) && !reachedBy.containsKey(next.to())) {
                    reachedBy.put(next.to(), next);
                    pending.add(next.to());
                }
            }
        }
        if (edge.from().equals(edge.to()) || !reachedBy.containsKey(edge.to())) {
            return null;
        }
        var path = new ArrayList<Edge>();
        for (String world = edge.to(); !world.equals(edge.from()); world = path.get(0).from()) {
            path.add(0, reachedBy.get(world));
        }
        return path;
    }

    /**
     * Proves the implication or negation {@code target} in a subproof: assumes its antecedent, runs {@code body} (which
     * gives the line of the consequent), brings the consequent to the last line, in the state of {@code target}, and
     * discharges the assumption with {@code ->I} or {@code -I}.
     *
     * @return the line of {@code target}
     */
    private int hypothetical(Context context, Labelled target, ToIntFunction<Context> body) {
        String state = context.state(target.world());
        ModalLogicalOperation antecedent;
        ModalLogicalOperation consequent;
        if (target.formula() instanceof ImplicationModal implication) {
            antecedent = implication.getLeft();
            consequent = implication.getRight();
        } else {
            antecedent = ((NegationModal) target.formula()).getElement();
            consequent = FALSE;
        }
        var inner = context.copy();
        inner.lines.put(new Labelled(target.world(), antecedent), apply(new ModalAssume(antecedent, state)));
        int line = body.applyAsInt(inner);
        if (consequent.equals(FALSE) && !proof.getSteps().get(line - 1).getState().equals(state) && target.formula() instanceof ImplicationModal) {
            apply(new ModalFE(line, FALSE, state));
        } else if (line != lastLine()) {
            apply(new ModalCopy(line));
        }
        return apply(target.formula() instanceof NegationModal ? new ModalNotI() : new ModalDeductionTheorem());
    }

    /**
     * Proves {@code target} by contradiction: assumes its negation, runs {@code body} (which gives the line of
     * {@code FALSE}), then {@code -I} and {@code -E}.
     */
    private int byContradiction(Context context, Labelled target, ToIntFunction<Context> body) {
        return apply(new ModalNotE(hypothetical(context, negate(negate(target)), body)));
    }

    /**
     * {@code FALSE} in the initial state, from {@code FALSE} at line {@code line}.
     */
    private int inInitialState(int line) {
        if (proof.getSteps().get(line - 1).getState().equals(proof.getState0())) {
            return line;
        }
        return apply(new ModalFE(line, FALSE, proof.getState0()));
    }

    /**
     * {@code target} from {@code FALSE} at line {@code falseLine}: that line itself when {@code target} is
     * {@code FALSE}, otherwise {@code FE}.
     */
    private int fromFalse(Context context, int falseLine, Labelled target) {
        if (target.formula().equals(FALSE)) {
            return falseLine;
        }
        return apply(new ModalFE(falseLine, target.formula(), context.state(target.world())));
    }

    /**
     * A state that no valid step uses yet.
     */
    private String freshState() {
        int i = 1;
        while (proof.stateIsUsedBefore("s" + i, proof.getSteps().size())) {
            i++;
        }
        return "s" + i;
    }

    private int apply(AbstractModalAction action) {
        if (!action.isValid(proof)) {
            throw new IllegalStateException("The modal solver built an invalid step: " + action.getClass().getSimpleName());
        }
        action.apply(proof);
        return lastLine();
    }

    private int lastLine() {
        return proof.getSteps().size();
    }
}
