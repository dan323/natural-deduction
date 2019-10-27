package com.dan323.expresions.classical;

import com.dan323.expresions.base.Conjunction;

import java.util.Map;

public final class ConjunctionClassic extends BinaryOperationClassic implements Conjunction {

    public ConjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return Conjunction.OPERATOR;
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) && getRight().evaluate(values);
    }

}
