package com.dan323.proof.firstorder.internal;

import com.dan323.expressions.firstorder.ConjunctionFirstOrder;
import com.dan323.expressions.firstorder.ConstantFirstOrder;
import com.dan323.expressions.firstorder.DisjunctionFirstOrder;
import com.dan323.expressions.firstorder.Equals;
import com.dan323.expressions.firstorder.Exists;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.expressions.firstorder.FunctionApplication;
import com.dan323.expressions.firstorder.ImplicationFirstOrder;
import com.dan323.expressions.firstorder.NegationFirstOrder;
import com.dan323.expressions.firstorder.Predicate;
import com.dan323.expressions.firstorder.Quantifier;
import com.dan323.expressions.firstorder.Term;
import com.dan323.expressions.firstorder.VariableTerm;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The automatic solver of first-order logic with equality: a bounded version of the intercalation calculus of Sieg
 * and Byrnes (<i>Normal natural deduction proofs (in classical logic)</i>, Studia Logica 60, 1998), the proof search of
 * the AProS project. It works backwards from the goal with the introduction rules and forwards from the available
 * lines with the elimination rules, and falls back on proof by contradiction. It is best effort: first-order logic is
 * undecidable, and when it finds nothing within its bounds it leaves the proof with its premises.
 *
 * <p>For a goal {@code G} and the lines available (the premises, the open assumptions and what they give), the search
 * <ol>
 *     <li>closes the available lines under the elimination rules that need no choice ({@link #saturate}): {@code &E},
 *     {@code ->E} when the antecedent is available, {@code -E} on a double negation, {@code FI} on {@code A} and
 *     {@code - A}, {@code ∀E} with every term of the proof (up to {@link #MAX_TERM_DEPTH}) and {@code =E} for the
 *     symmetry and transitivity of {@code =};</li>
 *     <li>is done when {@code G} (or {@code FALSE}, then {@code FE}) is among them;</li>
 *     <li>uses the introduction rule of {@code G} when it loses nothing: {@code &I}, {@code ->I}, {@code -I},
 *     {@code ∀I} with a new name, {@code =I} for {@code t = t};</li>
 *     <li>uses {@code ∃E} with a new name, then {@code |E}, on an available line (each once on a branch);</li>
 *     <li>tries each side of a disjunction goal ({@code |I}), and {@code ∃I} with each term of the proof;</li>
 *     <li>reaches the antecedent of an available implication, then goes on with its consequent ({@code ->E});</li>
 *     <li>for {@code FALSE}, reaches {@code A} for an available {@code - A} ({@code FI});</li>
 *     <li>otherwise assumes {@code - G} and reaches {@code FALSE} ({@code -I}, then {@code -E}).</li>
 * </ol>
 * The search is a tree of derivations, with iterative deepening on the steps 4 to 8, and is written down as steps only
 * once it succeeds ({@link Emitter}), so the proof only has rules of first-order logic and replays as any other.
 *
 * <p>It always stops: the steps 4 to 8 lower a depth that is at most {@link #MAX_DEPTH}, the introduction rules make
 * the goal smaller, the closure is bounded by {@link #MAX_ROUNDS}, {@link #MAX_TERMS} and {@link #MAX_LINES}, and the
 * whole search by {@link #CALL_BUDGET} goals and {@link #WORK_BUDGET} lines looked at by the closures (counts, not a
 * wall-clock limit, so the answer does not depend on the machine; on a laptop the budgets run out in about a second).
 */
public final class FirstOrderAutomate {

    /** The deepest iteration of the search. */
    static final int MAX_DEPTH = 7;
    /** How many goals the whole search may look at. */
    static final int CALL_BUDGET = 40_000;
    /** How many lines the closures under eliminations of the whole search may look at. */
    static final long WORK_BUDGET = 3_000_000;
    /**
     * How deep a term {@code ∀E} and {@code ∃I} use may be: {@code a} is 0, {@code f(a)} is 1. The terms written in the
     * premises and the goal are used however deep they are.
     */
    static final int MAX_TERM_DEPTH = 2;
    /** How many terms {@code ∀E} and {@code ∃I} use. */
    static final int MAX_TERMS = 8;
    /** How many rounds of the closure under eliminations. */
    static final int MAX_ROUNDS = 4;
    /** How many lines the closure may make available. */
    static final int MAX_LINES = 200;

    private static final FirstOrderOperation FALSE = ConstantFirstOrder.FALSE;

    private final FirstOrderNaturalDeduction proof;
    private final Set<String> initialNames = new HashSet<>();
    /** The terms written in the premises and the goal: not made by the search, so not bounded by their depth. */
    private final Set<Term> givenTerms = new HashSet<>();
    private Set<String> usedNames;
    private String defaultName;
    private int calls;
    private long work;

    public FirstOrderAutomate(FirstOrderNaturalDeduction proof) {
        this.proof = proof;
    }

    /**
     * Resets the proof to its premises and looks for a proof of its goal. When it finds none, the proof is left with
     * its premises.
     *
     * @return whether it found a proof
     */
    public boolean solve() {
        proof.reset();
        FirstOrderOperation goal = proof.getGoal();
        if (goal == null) {
            return false;
        }
        proof.getAssms().forEach(premise -> names(premise, initialNames));
        names(goal, initialNames);
        proof.getAssms().forEach(premise -> terms(premise, Set.of(), givenTerms));
        terms(goal, Set.of(), givenTerms);
        Map<FirstOrderOperation, Node> premises = new LinkedHashMap<>();
        for (FirstOrderOperation premise : proof.getAssms()) {
            premises.putIfAbsent(premise, new Node(Kind.PREMISE, premise, List.of(), null, null));
        }
        for (int depth = 0; depth <= MAX_DEPTH && calls < CALL_BUDGET && work < WORK_BUDGET; depth++) {
            usedNames = new HashSet<>(initialNames);
            defaultName = null;
            Node found = prove(premises, goal, depth, Set.of());
            if (found != null) {
                try {
                    new Emitter(proof).write(found);
                    return true;
                } catch (IllegalStateException e) {
                    // A derivation the rules do not accept: nothing is written down
                    proof.reset();
                    return false;
                }
            }
        }
        proof.reset();
        return false;
    }

    // ------------------------------------------------------------------ the search

    enum Kind {
        PREMISE, ASSUMPTION, AND_I, AND_E1, AND_E2, OR_I1, OR_I2, OR_E, IMP_I, IMP_E, NOT_I, NOT_E, FALSE_I, FALSE_E,
        FORALL_I, FORALL_E, EXISTS_I, EXISTS_E, EQ_I, EQ_E
    }

    /**
     * A derivation of {@code formula}.
     *
     * @param kind       the last rule
     * @param formula    what it derives
     * @param premises   the derivations it uses, in the order the rule cites them ({@code OR_E}: the disjunction and
     *                   the two implications; {@code EXISTS_E}: the existential and the subproof's conclusion;
     *                   {@code EQ_E}: the equation and the line it rewrites)
     * @param assumption the assumption a subproof opens with ({@code IMP_I}, {@code NOT_I}, {@code EXISTS_E})
     * @param term       the term of {@code FORALL_E} and {@code EQ_I}
     */
    record Node(Kind kind, FirstOrderOperation formula, List<Node> premises, FirstOrderOperation assumption, Term term) {
    }

    private static Node node(Kind kind, FirstOrderOperation formula, Node... premises) {
        return new Node(kind, formula, List.of(premises), null, null);
    }

    private static Node subproof(Kind kind, FirstOrderOperation formula, FirstOrderOperation assumption, Node... premises) {
        return new Node(kind, formula, List.of(premises), assumption, null);
    }

    private static Map<FirstOrderOperation, Node> with(Map<FirstOrderOperation, Node> known, FirstOrderOperation formula, Node node) {
        Map<FirstOrderOperation, Node> extended = new LinkedHashMap<>(known);
        extended.putIfAbsent(formula, node);
        return extended;
    }

    private static Map<FirstOrderOperation, Node> assuming(Map<FirstOrderOperation, Node> known, FirstOrderOperation assumption) {
        // The assumption replaces an equal line: inside the subproof it is the one to cite
        Map<FirstOrderOperation, Node> extended = new LinkedHashMap<>(known);
        extended.put(assumption, new Node(Kind.ASSUMPTION, assumption, List.of(), null, null));
        return extended;
    }

    private static <T> Set<T> plus(Set<T> set, T element) {
        Set<T> extended = new HashSet<>(set);
        extended.add(element);
        return extended;
    }

    /**
     * @param available  the lines available, with their derivations
     * @param goal       the formula to reach
     * @param depth      how many choices (steps 4 to 8 of the class comment) are left
     * @param eliminated the existentials and disjunctions already eliminated on this branch
     * @return a derivation of {@code goal} from {@code available}, or {@code null}
     */
    private Node prove(Map<FirstOrderOperation, Node> available, FirstOrderOperation goal, int depth,
                       Set<FirstOrderOperation> eliminated) {
        if (++calls > CALL_BUDGET || work > WORK_BUDGET) {
            return null;
        }
        Lines lines = new Lines(available, saturate(available, goal));
        Map<FirstOrderOperation, Node> known = lines.known();
        Node direct = known.get(goal);
        if (direct != null) {
            return direct;
        }
        Node absurd = known.get(FALSE);
        if (absurd != null) {
            return node(Kind.FALSE_E, goal, absurd);
        }
        Node intro = introduction(lines, goal, depth, eliminated);
        if (intro != null || isInvertible(goal)) {
            return intro;
        }
        if (depth <= 0) {
            return null;
        }
        Elimination elimination = eliminate(lines, goal, depth, eliminated);
        if (elimination != null) {
            return elimination.node();
        }
        Node found = sides(lines, goal, depth, eliminated);
        if (found == null) {
            found = backwards(lines, goal, depth, eliminated);
        }
        if (found == null && goal == FALSE) {
            found = refute(lines, depth, eliminated);
        }
        if (found == null) {
            found = byContradiction(lines, goal, depth, eliminated);
        }
        return found;
    }

    /**
     * The lines of a goal.
     *
     * @param available the lines it was given: the subgoals start from these and what they add, so the closure of
     *                  each subgoal uses the terms of that subgoal first
     * @param known     the available lines and their closure under eliminations
     */
    private record Lines(Map<FirstOrderOperation, Node> available, Map<FirstOrderOperation, Node> known) {
    }

    /**
     * The result of {@link #eliminate}: the derivation it found, or {@code null} if the rule it committed to failed.
     */
    private record Elimination(Node node) {
    }

    private static boolean isInvertible(FirstOrderOperation goal) {
        return goal instanceof ConjunctionFirstOrder || goal instanceof ImplicationFirstOrder
                || goal instanceof NegationFirstOrder || goal instanceof Forall
                || goal instanceof Equals(Term left, Term right) && left.equals(right);
    }

    /**
     * Step 3: the introduction rules that lose nothing.
     */
    private Node introduction(Lines lines, FirstOrderOperation goal, int depth,
                              Set<FirstOrderOperation> eliminated) {
        return switch (goal) {
            case ConjunctionFirstOrder and -> {
                Node left = prove(lines.available(), and.getLeft(), depth, eliminated);
                Node right = left == null ? null : prove(lines.available(), and.getRight(), depth, eliminated);
                yield right == null ? null : node(Kind.AND_I, goal, left, right);
            }
            case ImplicationFirstOrder imp -> {
                Node body = prove(assuming(lines.available(), imp.getLeft()), imp.getRight(), depth, eliminated);
                yield body == null ? null : subproof(Kind.IMP_I, goal, imp.getLeft(), body);
            }
            case NegationFirstOrder not -> {
                Node body = prove(assuming(lines.available(), not.getElement()), FALSE, depth, eliminated);
                yield body == null ? null : subproof(Kind.NOT_I, goal, not.getElement(), body);
            }
            case Forall forall -> {
                FirstOrderOperation instance = forall.getBody().substitute(forall.getVariable(), new VariableTerm(freshName()));
                Node body = prove(lines.available(), instance, depth, eliminated);
                yield body == null ? null : node(Kind.FORALL_I, goal, body);
            }
            case Equals(Term left, Term right) when left.equals(right) -> new Node(Kind.EQ_I, goal, List.of(), null, left);
            default -> null;
        };
    }

    /**
     * Step 4: {@code ∃E}, then {@code |E}, on an available line not yet eliminated on this branch. Eliminating them
     * loses nothing, so the search commits to the first one.
     *
     * @return {@code null} if there is no line to eliminate, else what the first one gave
     */
    private Elimination eliminate(Lines lines, FirstOrderOperation goal, int depth,
                           Set<FirstOrderOperation> eliminated) {
        for (var entry : lines.known().entrySet()) {
            if (entry.getKey() instanceof Exists exists && !eliminated.contains(exists)) {
                FirstOrderOperation witness = exists.getBody().substitute(exists.getVariable(), new VariableTerm(freshName()));
                Node body = prove(assuming(lines.available(), witness), goal, depth - 1, plus(eliminated, exists));
                return new Elimination(body == null ? null : subproof(Kind.EXISTS_E, goal, witness, entry.getValue(), body));
            }
        }
        for (var entry : lines.known().entrySet()) {
            if (entry.getKey() instanceof DisjunctionFirstOrder or && !eliminated.contains(or)) {
                Set<FirstOrderOperation> split = plus(eliminated, or);
                Node left = prove(assuming(lines.available(), or.getLeft()), goal, depth - 1, split);
                Node right = left == null ? null : prove(assuming(lines.available(), or.getRight()), goal, depth - 1, split);
                if (right == null) {
                    return new Elimination(null);
                }
                Node leftCase = subproof(Kind.IMP_I, new ImplicationFirstOrder(or.getLeft(), goal), or.getLeft(), left);
                Node rightCase = subproof(Kind.IMP_I, new ImplicationFirstOrder(or.getRight(), goal), or.getRight(), right);
                return new Elimination(node(Kind.OR_E, goal, entry.getValue(), leftCase, rightCase));
            }
        }
        return null;
    }

    /**
     * Step 5: a side of a disjunction goal, or an instance of an existential goal with a term of the proof.
     */
    private Node sides(Lines lines, FirstOrderOperation goal, int depth,
                       Set<FirstOrderOperation> eliminated) {
        if (goal instanceof DisjunctionFirstOrder or) {
            Node left = lines.known().get(or.getLeft());
            if (left != null) {
                return node(Kind.OR_I1, goal, left);
            }
            Node right = lines.known().get(or.getRight());
            if (right != null) {
                return node(Kind.OR_I2, goal, right);
            }
            left = prove(lines.available(), or.getLeft(), depth - 1, eliminated);
            if (left != null) {
                return node(Kind.OR_I1, goal, left);
            }
            right = prove(lines.available(), or.getRight(), depth - 1, eliminated);
            return right == null ? null : node(Kind.OR_I2, goal, right);
        }
        if (goal instanceof Exists exists) {
            List<FirstOrderOperation> instances = universe(lines.known().keySet(), goal, true).stream()
                    .map(term -> exists.getBody().substitute(exists.getVariable(), term))
                    .distinct()
                    .toList();
            for (FirstOrderOperation instance : instances) {
                Node found = lines.known().get(instance);
                if (found != null) {
                    return node(Kind.EXISTS_I, goal, found);
                }
            }
            for (FirstOrderOperation instance : instances) {
                Node found = prove(lines.available(), instance, depth - 1, eliminated);
                if (found != null) {
                    return node(Kind.EXISTS_I, goal, found);
                }
            }
        }
        return null;
    }

    /**
     * Step 6: reach the antecedent {@code A} of an available {@code A -> B} whose {@code B} is not available, then
     * reach the goal with {@code B} too.
     */
    private Node backwards(Lines lines, FirstOrderOperation goal, int depth,
                           Set<FirstOrderOperation> eliminated) {
        for (var entry : List.copyOf(lines.known().entrySet())) {
            if (entry.getKey() instanceof ImplicationFirstOrder imp && !lines.known().containsKey(imp.getRight())
                    && !imp.getLeft().equals(goal)) {
                Node antecedent = prove(lines.available(), imp.getLeft(), depth - 1, eliminated);
                if (antecedent != null) {
                    Node consequent = node(Kind.IMP_E, imp.getRight(), entry.getValue(), antecedent);
                    Node found = prove(with(lines.available(), imp.getRight(), consequent), goal, depth - 1, eliminated);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Step 7: {@code FALSE} from an available {@code - A} and {@code A}.
     */
    private Node refute(Lines lines, int depth, Set<FirstOrderOperation> eliminated) {
        for (var entry : List.copyOf(lines.known().entrySet())) {
            if (entry.getKey() instanceof NegationFirstOrder not && not.getElement() != FALSE) {
                Node positive = prove(lines.available(), not.getElement(), depth - 1, eliminated);
                if (positive != null) {
                    return node(Kind.FALSE_I, FALSE, positive, entry.getValue());
                }
            }
        }
        return null;
    }

    /**
     * Step 8: assume {@code - G}, reach {@code FALSE}, then {@code -I} and {@code -E}. Not when {@code - G} is
     * already available, which would only repeat the attempt that is going on.
     */
    private Node byContradiction(Lines lines, FirstOrderOperation goal, int depth,
                                 Set<FirstOrderOperation> eliminated) {
        if (goal == FALSE) {
            return null;
        }
        FirstOrderOperation negated = new NegationFirstOrder(goal);
        if (lines.known().containsKey(negated)) {
            return null;
        }
        Node body = prove(assuming(lines.available(), negated), FALSE, depth - 1, eliminated);
        if (body == null) {
            return null;
        }
        Node doubleNegation = subproof(Kind.NOT_I, new NegationFirstOrder(negated), negated, body);
        return node(Kind.NOT_E, goal, doubleNegation);
    }

    // ------------------------------------------------------------------ the closure under eliminations

    /**
     * Step 1: the available lines and what the elimination rules that need no choice give from them.
     */
    private Map<FirstOrderOperation, Node> saturate(Map<FirstOrderOperation, Node> available, FirstOrderOperation goal) {
        Map<FirstOrderOperation, Node> known = new LinkedHashMap<>(available);
        for (int round = 0; round < MAX_ROUNDS; round++) {
            List<Term> terms = universe(known.keySet(), goal, false);
            List<Map.Entry<FirstOrderOperation, Node>> lines = List.copyOf(known.entrySet());
            int before = known.size();
            // The rules that add a line or two first, then ∀E, which adds a line per term and stops at MAX_LINES
            for (var line : lines) {
                if (known.size() >= 2 * MAX_LINES) {
                    return known;
                }
                if (!(line.getKey() instanceof Forall)) {
                    eliminations(known, line.getKey(), line.getValue(), terms, lines);
                }
            }
            for (var line : lines) {
                if (known.size() < MAX_LINES && line.getKey() instanceof Forall) {
                    eliminations(known, line.getKey(), line.getValue(), terms, lines);
                }
            }
            if (known.size() == before) {
                break;
            }
        }
        return known;
    }

    private void eliminations(Map<FirstOrderOperation, Node> known, FirstOrderOperation formula, Node from,
                              List<Term> terms, List<Map.Entry<FirstOrderOperation, Node>> lines) {
        work++;
        switch (formula) {
            case ConjunctionFirstOrder and -> {
                known.putIfAbsent(and.getLeft(), node(Kind.AND_E1, and.getLeft(), from));
                known.putIfAbsent(and.getRight(), node(Kind.AND_E2, and.getRight(), from));
            }
            case ImplicationFirstOrder imp -> {
                Node antecedent = known.get(imp.getLeft());
                if (antecedent != null) {
                    known.putIfAbsent(imp.getRight(), node(Kind.IMP_E, imp.getRight(), from, antecedent));
                }
            }
            case NegationFirstOrder not -> {
                if (not.getElement() instanceof NegationFirstOrder inner) {
                    known.putIfAbsent(inner.getElement(), node(Kind.NOT_E, inner.getElement(), from));
                }
                Node positive = known.get(not.getElement());
                if (positive != null) {
                    known.putIfAbsent(FALSE, node(Kind.FALSE_I, FALSE, positive, from));
                }
            }
            case Forall forall -> {
                if (!forall.getBody().freeVariables().contains(forall.getVariable())) {
                    known.putIfAbsent(forall.getBody(),
                            new Node(Kind.FORALL_E, forall.getBody(), List.of(from), null, new VariableTerm(forall.getVariable())));
                } else {
                    work += terms.size();
                    for (Term term : terms) {
                        FirstOrderOperation instance = forall.getBody().substitute(forall.getVariable(), term);
                        known.putIfAbsent(instance, new Node(Kind.FORALL_E, instance, List.of(from), null, term));
                    }
                }
            }
            case Equals(Term left, Term right) when !left.equals(right) -> {
                // Symmetry: =E on a = b and a = a, replacing the first a
                Equals symmetric = new Equals(right, left);
                known.putIfAbsent(symmetric, node(Kind.EQ_E, symmetric, from,
                        new Node(Kind.EQ_I, new Equals(left, left), List.of(), null, left)));
                // Transitivity: =E on b = c and a = b, replacing b
                work += lines.size();
                for (var line : lines) {
                    if (line.getKey() instanceof Equals(Term middle, Term end) && middle.equals(right) && !end.equals(right)) {
                        Equals chain = new Equals(left, end);
                        known.putIfAbsent(chain, node(Kind.EQ_E, chain, line.getValue(), from));
                    }
                }
            }
            default -> {
                // Nothing to eliminate without a choice
            }
        }
    }

    // ------------------------------------------------------------------ terms and names

    /**
     * @param formulas the available lines
     * @param goal     the goal
     * @param forGoal  whether the terms are for an existential goal (else for the universals among {@code formulas})
     * @return the terms {@code ∀E} and {@code ∃I} use: those free in the goal, then in the lines, up to
     * {@link #MAX_TERM_DEPTH} (unless written in the premises or the goal) and {@link #MAX_TERMS}; a new name when
     * there is none and one is needed
     */
    private List<Term> universe(Set<FirstOrderOperation> formulas, FirstOrderOperation goal, boolean forGoal) {
        Set<Term> terms = new LinkedHashSet<>();
        terms(goal, Set.of(), terms);
        formulas.forEach(formula -> terms(formula, Set.of(), terms));
        List<Term> universe = terms.stream().filter(term -> depth(term) <= MAX_TERM_DEPTH || givenTerms.contains(term)).limit(MAX_TERMS).toList();
        if (universe.isEmpty() && (forGoal || formulas.stream().anyMatch(Forall.class::isInstance))) {
            // The domain is not empty: a name nothing is said about stands for any element
            if (defaultName == null) {
                defaultName = freshName();
            }
            return List.of(new VariableTerm(defaultName));
        }
        return universe;
    }

    private static void terms(FirstOrderOperation formula, Set<String> bound, Set<Term> terms) {
        switch (formula) {
            case ConjunctionFirstOrder and -> {
                terms(and.getLeft(), bound, terms);
                terms(and.getRight(), bound, terms);
            }
            case DisjunctionFirstOrder or -> {
                terms(or.getLeft(), bound, terms);
                terms(or.getRight(), bound, terms);
            }
            case ImplicationFirstOrder imp -> {
                terms(imp.getLeft(), bound, terms);
                terms(imp.getRight(), bound, terms);
            }
            case NegationFirstOrder not -> terms(not.getElement(), bound, terms);
            case Predicate predicate -> predicate.getArguments().forEach(term -> subterms(term, bound, terms));
            case Equals(Term left, Term right) -> {
                subterms(left, bound, terms);
                subterms(right, bound, terms);
            }
            case Quantifier quantifier -> terms(quantifier.getBody(), plus(bound, quantifier.getVariable()), terms);
            case ConstantFirstOrder constant -> {
                // No terms
            }
        }
    }

    private static void subterms(Term term, Set<String> bound, Set<Term> terms) {
        if (term instanceof FunctionApplication(String name, List<Term> arguments)) {
            arguments.forEach(argument -> subterms(argument, bound, terms));
        }
        if (term.freeVariables().stream().noneMatch(bound::contains)) {
            terms.add(term);
        }
    }

    private static int depth(Term term) {
        return term instanceof FunctionApplication(String name, List<Term> arguments)
                ? 1 + arguments.stream().mapToInt(FirstOrderAutomate::depth).max().orElse(0)
                : 0;
    }

    /**
     * @return a name used nowhere in the premises, the goal or the search so far, so it can be generalized over
     */
    private String freshName() {
        for (int index = 0; ; index++) {
            for (char letter = 'a'; letter <= 'w'; letter++) {
                String name = index == 0 ? String.valueOf(letter) : letter + String.valueOf(index);
                if (usedNames.add(name)) {
                    return name;
                }
            }
        }
    }

    private static void names(FirstOrderOperation formula, Set<String> names) {
        switch (formula) {
            case ConjunctionFirstOrder and -> {
                names(and.getLeft(), names);
                names(and.getRight(), names);
            }
            case DisjunctionFirstOrder or -> {
                names(or.getLeft(), names);
                names(or.getRight(), names);
            }
            case ImplicationFirstOrder imp -> {
                names(imp.getLeft(), names);
                names(imp.getRight(), names);
            }
            case NegationFirstOrder not -> names(not.getElement(), names);
            case Predicate predicate -> {
                names.add(predicate.getName());
                predicate.getArguments().forEach(term -> names(term, names));
            }
            case Equals(Term left, Term right) -> {
                names(left, names);
                names(right, names);
            }
            case Quantifier quantifier -> {
                names.add(quantifier.getVariable());
                names(quantifier.getBody(), names);
            }
            case ConstantFirstOrder constant -> {
                // No names
            }
        }
    }

    private static void names(Term term, Set<String> names) {
        switch (term) {
            case VariableTerm(String name) -> names.add(name);
            case FunctionApplication(String name, List<Term> arguments) -> {
                names.add(name);
                arguments.forEach(argument -> names(argument, names));
            }
        }
    }
}
