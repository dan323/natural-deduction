package com.dan323.expressions.classical;

import com.dan323.expressions.base.Variable;
import com.dan323.expressions.classical.exceptions.InvalidMapValuesException;

import java.util.Map;

public final class VariableClassic extends Variable implements ClassicalLogicOperation {

    public VariableClassic(String var) {
        super(var);
    }

    public boolean evaluate(Map<String, Boolean> values) {
        if (values.containsKey(toString())) {
            return values.get(toString());
        }
        throw new InvalidMapValuesException("The map " + values + " gives no value to " + toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode() * 3 + getClass().hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableClassic varClass) {
            return toString().equals(varClass.toString());
        } else {
            return false;
        }
    }

}
