package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The equation {@code left = right} between two terms. As a formula it is not symmetric: {@code a = b} and
 * {@code b = a} are different formulas.
 */
public final class Equals implements FirstOrderOperation {

    private final Term left;
    private final Term right;

    public Equals(Term left, Term right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    public Term getLeft() {
        return left;
    }

    public Term getRight() {
        return right;
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
        return Alpha.equal(this, obj);
    }

    @Override
    public int hashCode() {
        return Alpha.hash(this);
    }
}
