package com.dan323.expressions.classical;

import com.dan323.expressions.base.Negation;

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
        return getElement().hashCode() * 3 - 11 * getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NegationClassic neg) {
            return this.getElement().equals(neg.getElement());
        } else {
            return false;
        }
    }

}
