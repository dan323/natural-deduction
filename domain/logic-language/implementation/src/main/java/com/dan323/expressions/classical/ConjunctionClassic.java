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
        return getLeft().hashCode() * 3 - getRight().hashCode() * 13 + getClass().hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConjunctionClassic conjClass) {
            return conjClass.getLeft().equals(getLeft()) && conjClass.getRight().equals(getRight());
        } else {
            return false;
        }
    }
}
