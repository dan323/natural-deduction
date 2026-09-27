package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.Implication;

import java.util.HashSet;
import java.util.Set;

public final class ImplicationFirstOrder extends Implication<FirstOrderOperation> implements FirstOrderOperation {

    public ImplicationFirstOrder(FirstOrderOperation left, FirstOrderOperation right) {
        super(left, right);
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>(getLeft().freeVariables());
        free.addAll(getRight().freeVariables());
        return Set.copyOf(free);
    }

    @Override
    public ImplicationFirstOrder substitute(String variable, Term term) {
        return new ImplicationFirstOrder(getLeft().substitute(variable, term), getRight().substitute(variable, term));
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
