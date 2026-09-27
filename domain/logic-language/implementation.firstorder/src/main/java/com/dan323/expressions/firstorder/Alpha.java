package com.dan323.expressions.firstorder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Equality up to renaming of bound variables. Two formulas are alpha-equivalent exactly when their canonical forms are
 * equal: a canonical form names every bound variable after the depth of its binder ({@code #0}, {@code #1}, ...), which
 * no identifier can clash with, and drops the name the binder gave it.
 */
final class Alpha {

    private Alpha() {
    }

    static boolean equal(FirstOrderOperation formula, Object other) {
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
            case ImplicationFirstOrder i -> binary("->", i.getLeft(), i.getRight(), bound, depth);
            case NegationFirstOrder n -> "-(" + canonical(n.getElement(), bound, depth) + ")";
            case ConstantFirstOrder k -> "$" + k.name();
            case Predicate p -> p.getName() + p.getArguments().stream()
                    .map(t -> canonical(t, bound))
                    .collect(Collectors.joining(",", "(", ")"));
            case Equals e -> "=(" + canonical(e.getLeft(), bound) + "," + canonical(e.getRight(), bound) + ")";
            case Quantifier q -> {
                Map<String, String> inner = new HashMap<>(bound);
                inner.put(q.getVariable(), "#" + depth);
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
            case VariableTerm v -> bound.getOrDefault(v.name(), v.name());
            case FunctionApplication f -> f.name() + f.arguments().stream()
                    .map(t -> canonical(t, bound))
                    .collect(Collectors.joining(",", "(", ")"));
            case Product p -> "*(" + canonical(p.left(), bound) + "," + canonical(p.right(), bound) + ")";
        };
    }

    /**
     * @param name  the name to start from
     * @param avoid the names the result must not be
     * @return {@code name} with its trailing digits replaced by the smallest positive number that gives a name not in
     * {@code avoid}
     */
    static String fresh(String name, Set<String> avoid) {
        String base = name.replaceAll("\\d+$", "");
        int i = 1;
        while (avoid.contains(base + i)) {
            i++;
        }
        return base + i;
    }
}
