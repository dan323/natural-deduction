package com.dan323.expressions.classical;

import com.dan323.expressions.base.Conjunction;

import java.util.Map;

public final class ConjunctionClassic extends Conjunction<ClassicalLogicOperation> implements ClassicalLogicOperation {

    public ConjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) && getRight().evaluate(values);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 3 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ClassicalLogicOperation.classicalOperationEquals(obj, super::equals);
    }

}
