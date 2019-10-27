package com.dan323.expresions.classical;

import com.dan323.expresions.base.Disjunction;

import java.util.Map;

public final class DisjunctionClassic extends BinaryOperationClassic implements Disjunction {

    public DisjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return Disjunction.OPERATOR;
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) || getRight().evaluate(values);
    }

}