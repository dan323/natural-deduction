package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The equation {@code left = right} between two terms. As a formula it is not symmetric: {@code a = b} and
 * {@code b = a} are different formulas.
 *
 * @param left  the left-hand term
 * @param right the right-hand term
 */
public record Equals(Term left, Term right) implements FirstOrderOperation {

    public Equals {
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
    public Equals substitute(String variable, Term term) {
        return new Equals(left.substitute(variable, term), right.substitute(variable, term));
    }

    @Override
    public String toString() {
        return left + " = " + right;
    }

    @Override
    public boolean equals(Object obj) {
        return Alpha.equivalent(this, obj);
    }

    @Override
    public int hashCode() {
        return Alpha.hash(this);
    }
}
