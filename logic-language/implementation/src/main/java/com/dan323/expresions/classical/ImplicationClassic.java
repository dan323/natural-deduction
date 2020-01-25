package com.dan323.expresions.classical;

import com.dan323.expresions.base.Implication;

import java.util.Map;

public final class ImplicationClassic extends Implication<ClassicalLogicOperation> implements ClassicalLogicOperation {


    public ImplicationClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return !getLeft().evaluate(values) || getRight().evaluate(values);
    }

    @Override
    public boolean equals(Object obj) {
        return ClassicalLogicOperation.classicalOperationEquals(obj, super::equals);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 3 + getClass().hashCode();
    }
}
