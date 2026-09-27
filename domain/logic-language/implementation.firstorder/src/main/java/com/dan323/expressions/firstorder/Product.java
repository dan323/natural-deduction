package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The infix product {@code left*right}. The parser reads {@code *} as left-associative; the printer puts parentheses
 * around every operand that is itself a product, so {@code (x*y)*z} and {@code x*(y*z)} print as written.
 *
 * @param left  the left factor
 * @param right the right factor
 */
public record Product(Term left, Term right) implements Term {

    public Product {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>(left.freeVariables());
        free.addAll(right.freeVariables());
        return Set.copyOf(free);
    }

    @Override
    public Term substitute(String variable, Term term) {
        return new Product(left.substitute(variable, term), right.substitute(variable, term));
    }

    @Override
    public String toString() {
        return factor(left) + "*" + factor(right);
    }

    private static String factor(Term term) {
        return term instanceof Product ? "(" + term + ")" : term.toString();
    }
}
