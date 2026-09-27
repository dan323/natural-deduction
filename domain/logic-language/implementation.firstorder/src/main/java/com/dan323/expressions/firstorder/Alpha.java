package com.dan323.expressions.firstorder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Equality up to renaming of bound variables. Two formulas are alpha-equivalent exactly when their canonical forms are
 * equal. The canonical form is an injective encoding of the formula with every bound variable replaced by the depth of
 * its binder: each node starts with a tag of its own kind ({@code P} predicate, {@code A}/{@code E} quantifier,
 * {@code V} free variable, {@code B} bound variable, {@code F} function application, ...) and every name is written
 * with its length in front, so no name, whatever characters it holds, can pass for another node or another split of
 * the arguments.
 */
final class Alpha {

    private Alpha() {
    }

    static boolean equivalent(FirstOrderOperation formula, Object other) {
        return other instanceof FirstOrderOperation that && canonical(formula).equals(canonical(that));
    }

    static int hash(FirstOrderOperation formula) {
        return canonical(formula).hashCode();
    }

    static String canonical(FirstOrderOperation formula) {
        return canonical(formula, Map.of(), 0);
    }

    private static String canonical(FirstOrderOperation formula, Map<String, String> bound, int depth) {
        return switch (formula) {
            case ConjunctionFirstOrder c -> binary("&", c.getLeft(), c.getRight(), bound, depth);
            case DisjunctionFirstOrder d -> binary("|", d.getLeft(), d.getRight(), bound, depth);
            case ImplicationFirstOrder i -> binary(">", i.getLeft(), i.getRight(), bound, depth);
            case NegationFirstOrder n -> "-(" + canonical(n.getElement(), bound, depth) + ")";
            case ConstantFirstOrder k -> "$" + k.name();
            case Predicate p -> "P" + name(p.getName()) + arguments(p.getArguments(), bound);
            case Equals(Term left, Term right) -> "=(" + canonical(left, bound) + "," + canonical(right, bound) + ")";
            case Quantifier q -> {
                Map<String, String> inner = new HashMap<>(bound);
                inner.put(q.getVariable(), "B" + depth + ";");
                yield q.getSymbol() + "(" + canonical(q.getBody(), inner, depth + 1) + ")";
            }
        };
    }

    private static String binary(String operator, FirstOrderOperation left, FirstOrderOperation right,
                                 Map<String, String> bound, int depth) {
        return operator + "(" + canonical(left, bound, depth) + "," + canonical(right, bound, depth) + ")";
    }

    private static String canonical(Term term, Map<String, String> bound) {
        return switch (term) {
            case VariableTerm(String name) -> bound.getOrDefault(name, "V" + name(name));
            case FunctionApplication(String name, List<Term> arguments) -> "F" + name(name) + arguments(arguments, bound);
        };
    }

    private static String arguments(List<Term> arguments, Map<String, String> bound) {
        return arguments.stream()
                .map(t -> canonical(t, bound))
                .collect(Collectors.joining(",", "(", ")"));
    }

    /**
     * @return {@code name} preceded by its length, so it can be read back without knowing which characters follow it
     */
    private static String name(String name) {
        return name.length() + ":" + name;
    }

    /**
     * @param name  the name to start from
     * @param avoid the names the result must not be
     * @return {@code name} with its trailing digits replaced by the smallest positive number that gives a name not in
     * {@code avoid}
     */
    static String fresh(String name, Set<String> avoid) {
        int end = name.length();
        while (end > 0 && isAsciiDigit(name.charAt(end - 1))) {
            end--;
        }
        String base = name.substring(0, end);
        int i = 1;
        while (avoid.contains(base + i)) {
            i++;
        }
        return base + i;
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
