package com.dan323.expresions.clasical;

import com.dan323.expresions.LogicOperation;
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
    public boolean evaluate(Map<String, Boolean> values){
        return !getLeft().evaluate(values) || getRight().evaluate(values);
    }

    @Override
    public Implication construct(LogicOperation left, LogicOperation right) {
        if (left instanceof ClassicalLogicOperation && right instanceof ClassicalLogicOperation){
            return new ImplicationClassic((ClassicalLogicOperation) left,(ClassicalLogicOperation)right);
        }
        throw new IllegalArgumentException();
    }
}
