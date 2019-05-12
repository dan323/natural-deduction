package com.dan323.expresions.clasical;

import com.dan323.expresions.util.Conjuntion;

import java.util.Map;

public final class ConjuntionClassic extends BinaryOperationClassic implements Conjuntion {

    public ConjuntionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "&";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) && getRight().evaluate(values);
    }

}
