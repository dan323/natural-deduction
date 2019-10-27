package com.dan323.expresions.classical;

import com.dan323.expresions.base.Negation;

import java.util.Map;

public final class NegationClassic extends UnaryOperationClassic implements Negation {

    public NegationClassic(ClassicalLogicOperation elem) {
        super(elem);
    }

    @Override
    protected String getOperator() {
        return Negation.OPERATOR;
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return !getElement().evaluate(values);
    }

}
