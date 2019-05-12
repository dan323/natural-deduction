package com.dan323.expresions.clasical;

import com.dan323.expresions.util.Implication;

import java.util.Map;

public final class ImplicationClassic extends BinaryOperationClassic implements Implication {


    public ImplicationClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "->";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return !getLeft().evaluate(values) || getRight().evaluate(values);
    }

}
