package com.dan323.proof.firstorder;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.firstorder.ConjunctionFirstOrder;
import com.dan323.expressions.firstorder.ConstantFirstOrder;
import com.dan323.expressions.firstorder.DisjunctionFirstOrder;
import com.dan323.expressions.firstorder.Equals;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.FunctionApplication;
import com.dan323.expressions.firstorder.ImplicationFirstOrder;
import com.dan323.expressions.firstorder.NegationFirstOrder;
import com.dan323.expressions.firstorder.Predicate;
import com.dan323.expressions.firstorder.Quantifier;
import com.dan323.expressions.firstorder.Term;
import com.dan323.expressions.firstorder.VariableTerm;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Checks on substitution instances, the side conditions of the quantifier and equality rules.
 * <p>
 * Both checks walk two formulas side by side. Under a quantifier, both bound variables are renamed to one name that is
 * free nowhere in the formulas or the terms involved, so a term only matches where its names are free, and a name
 * that a quantifier would capture never matches.
 */
public final class Instances {

    private static final String FRESH_BASE = "v";

    private Instances() {
        throw new UnsupportedOperationException();
    }

    /**
     * The result of {@link #instanceOf}: whether {@code instance} is {@code body[x:=t]}, and for which {@code t}.
     *
     * @param matches whether {@code instance} is an instance of the body
     * @param term    the term {@code t}, or {@code null} when {@code x} is not free in the body (any term gives it)
     */
    public record Match(boolean matches, Term term) {

        private static final Match NONE = new Match(false, null);

        public boolean isVacuous() {
            return matches && term == null;
        }
    }

    /**
     * @param body     the body {@code A} of a quantifier
     * @param variable the variable {@code x} it binds
     * @param instance the candidate formula
     * @return whether {@code instance} is {@code A[x:=t]} for some term {@code t}, and that term
     */
    public static Match instanceOf(FirstOrderOperation body, String variable, FirstOrderOperation instance) {
        if (!body.freeVariables().contains(variable)) {
            return body.equals(instance) ? new Match(true, null) : Match.NONE;
        }
        Term[] found = new Term[1];
        BiPredicate<Term, Term> bind = (a, b) -> {
            if (a instanceof VariableTerm(String name) && name.equals(variable)) {
                if (found[0] == null) {
                    found[0] = b;
                }
                return found[0].equals(b);
            }
            return false;
        };
        Set<String> avoid = new HashSet<>(body.freeVariables());
        avoid.addAll(instance.freeVariables());
        if (!correspond(body, instance, bind, avoid, variable) || found[0] == null) {
            return Match.NONE;
        }
        // The walk renames bound variables to names that are free nowhere, so a term that picked one up would be
        // captured: substituting it back does not give the instance, and the match is refused.
        return body.substitute(variable, found[0]).equals(instance) ? new Match(true, found[0]) : Match.NONE;
    }

    /**
     * @param from   the formula {@code A}
     * @param to     the candidate formula
     * @param source the term {@code s}
     * @param target the term {@code t}
     * @return whether {@code to} is {@code A} with some (maybe none, maybe all) of the free occurrences of {@code s}
     * replaced by {@code t}, no name of {@code t} being captured by a quantifier of {@code A}
     */
    public static boolean isReplacement(FirstOrderOperation from, FirstOrderOperation to, Term source, Term target) {
        Set<String> avoid = new HashSet<>(from.freeVariables());
        avoid.addAll(to.freeVariables());
        avoid.addAll(source.freeVariables());
        avoid.addAll(target.freeVariables());
        return correspond(from, to, (a, b) -> a.equals(source) && b.equals(target), avoid, null);
    }

    /**
     * @param a      the formula on the left
     * @param b      the formula on the right
     * @param leaf   decides a pair of terms that are not equal and are not two applications of the same function
     * @param avoid  names a renamed bound variable must not take
     * @param stop   a name that, once a quantifier on the left binds it, makes the rest of that side need to be equal
     *               (the variable of {@link #instanceOf}), or {@code null}
     * @return whether the formulas have the same shape and every pair of terms in the same place is accepted
     */
    private static boolean correspond(FirstOrderOperation a, FirstOrderOperation b, BiPredicate<Term, Term> leaf,
                                      Set<String> avoid, String stop) {
        return switch (a) {
            case ConjunctionFirstOrder c -> b instanceof ConjunctionFirstOrder d && binary(c, d, leaf, avoid, stop);
            case DisjunctionFirstOrder c -> b instanceof DisjunctionFirstOrder d && binary(c, d, leaf, avoid, stop);
            case ImplicationFirstOrder c -> b instanceof ImplicationFirstOrder d && binary(c, d, leaf, avoid, stop);
            case NegationFirstOrder n ->
                    b instanceof NegationFirstOrder m && correspond(n.getElement(), m.getElement(), leaf, avoid, stop);
            case ConstantFirstOrder k -> k == b;
            case Predicate p -> b instanceof Predicate q && p.getName().equals(q.getName())
                    && terms(p.getArguments(), q.getArguments(), leaf);
            case Equals(Term left, Term right) ->
                    b instanceof Equals(Term otherLeft, Term otherRight) && term(left, otherLeft, leaf) && term(right, otherRight, leaf);
            case Quantifier q -> b instanceof Quantifier r && q.getClass().equals(r.getClass()) && quantifier(q, r, leaf, avoid, stop);
        };
    }

    private static boolean binary(BinaryOperation<FirstOrderOperation> a, BinaryOperation<FirstOrderOperation> b,
                                  BiPredicate<Term, Term> leaf, Set<String> avoid, String stop) {
        return correspond(a.getLeft(), b.getLeft(), leaf, avoid, stop)
                && correspond(a.getRight(), b.getRight(), leaf, avoid, stop);
    }

    private static boolean quantifier(Quantifier a, Quantifier b, BiPredicate<Term, Term> leaf, Set<String> avoid, String stop) {
        if (a.getVariable().equals(stop)) {
            // The variable is bound here, so nothing below is an occurrence of it.
            return a.equals(b);
        }
        Set<String> inner = new HashSet<>(avoid);
        String fresh = fresh(inner);
        inner.add(fresh);
        VariableTerm renamed = new VariableTerm(fresh);
        return correspond(a.getBody().substitute(a.getVariable(), renamed), b.getBody().substitute(b.getVariable(), renamed),
                leaf, inner, stop);
    }

    private static boolean terms(List<Term> a, List<Term> b, BiPredicate<Term, Term> leaf) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!term(a.get(i), b.get(i), leaf)) {
                return false;
            }
        }
        return true;
    }

    private static boolean term(Term a, Term b, BiPredicate<Term, Term> leaf) {
        if (leaf.test(a, b)) {
            return true;
        }
        if (a instanceof FunctionApplication(String name, List<Term> arguments)
                && b instanceof FunctionApplication(String otherName, List<Term> otherArguments)
                && name.equals(otherName) && terms(arguments, otherArguments, leaf)) {
            return true;
        }
        return a.equals(b);
    }

    /**
     * @param avoid names in use
     * @return a name not in {@code avoid}
     */
    static String fresh(Set<String> avoid) {
        int i = 0;
        while (avoid.contains(FRESH_BASE + i)) {
            i++;
        }
        return FRESH_BASE + i;
    }

    /**
     * @param term a term
     * @return the name {@code term} is, if it is a bare name (a variable or constant), and not a function application
     */
    public static Optional<String> name(Term term) {
        return term instanceof VariableTerm(String name) ? Optional.of(name) : Optional.empty();
    }
}
