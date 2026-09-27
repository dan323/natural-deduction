package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.Disjunction;

import java.util.HashSet;
import java.util.Set;

public final class DisjunctionFirstOrder extends Disjunction<FirstOrderOperation> implements FirstOrderOperation {

    public DisjunctionFirstOrder(FirstOrderOperation left, FirstOrderOperation right) {
        super(left, right);
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>(getLeft().freeVariables());
        free.addAll(getRight().freeVariables());
        return Set.copyOf(free);
    }

    @Override
    public DisjunctionFirstOrder substitute(String variable, Term term) {
        return new DisjunctionFirstOrder(getLeft().substitute(variable, term), getRight().substitute(variable, term));
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
