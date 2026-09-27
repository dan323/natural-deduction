package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.Conjunction;

import java.util.HashSet;
import java.util.Set;

public final class ConjunctionFirstOrder extends Conjunction<FirstOrderOperation> implements FirstOrderOperation {

    public ConjunctionFirstOrder(FirstOrderOperation left, FirstOrderOperation right) {
        super(left, right);
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>(getLeft().freeVariables());
        free.addAll(getRight().freeVariables());
        return Set.copyOf(free);
    }

    @Override
    public ConjunctionFirstOrder substitute(String variable, Term term) {
        return new ConjunctionFirstOrder(getLeft().substitute(variable, term), getRight().substitute(variable, term));
    }

    @Override
    public String toString() {
        return Printer.binary(getLeft(), getOperator(), getRight());
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
