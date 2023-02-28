package com.dan323.expressions.classical;

import com.dan323.expressions.base.Implication;

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
        if (obj instanceof ImplicationClassic impClass) {
            return impClass.getLeft().equals(getLeft()) && impClass.getRight().equals(getRight());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getRight().hashCode() * 3 - 5 * getLeft().hashCode() + 33 * getClass().hashCode();
    }
}
