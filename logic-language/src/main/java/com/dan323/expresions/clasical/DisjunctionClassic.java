package com.dan323.expresions.clasical;

import com.dan323.expresions.util.Disjunction;

import java.util.Map;

public final class DisjunctionClassic extends BinaryOperationClassic implements Disjunction {

    public DisjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }


    @Override
    protected String getOperator() {
        return "|";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) || getRight().evaluate(values);
    }

}