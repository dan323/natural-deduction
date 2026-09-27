package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.Negation;

import java.util.Set;

public final class NegationFirstOrder extends Negation<FirstOrderOperation> implements FirstOrderOperation {

    public NegationFirstOrder(FirstOrderOperation element) {
        super(element);
    }

    @Override
    public Set<String> freeVariables() {
        return getElement().freeVariables();
    }

    @Override
    public NegationFirstOrder substitute(String variable, Term term) {
        return new NegationFirstOrder(getElement().substitute(variable, term));
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
