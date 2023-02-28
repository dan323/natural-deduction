package com.dan323.expressions.classical;

import com.dan323.expressions.base.Disjunction;

import java.util.Map;

public final class DisjunctionClassic extends Disjunction<ClassicalLogicOperation> implements ClassicalLogicOperation {

    public DisjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) || getRight().evaluate(values);
    }

    @Override
    public int hashCode() {
        return getRight().hashCode() * 3 + 7 * getLeft().hashCode() - 11 * getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DisjunctionClassic disjClass) {
            return disjClass.getLeft().equals(getLeft()) && disjClass.getRight().equals(getRight());
        } else {
            return false;
        }
    }

}