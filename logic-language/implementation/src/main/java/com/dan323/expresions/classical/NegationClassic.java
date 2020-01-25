package com.dan323.expresions.classical;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;

import java.util.Map;

public final class NegationClassic extends Negation<ClassicalLogicOperation> implements ClassicalLogicOperation {

    public NegationClassic(ClassicalLogicOperation elem) {
        super(elem);
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return !getElement().evaluate(values);
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
