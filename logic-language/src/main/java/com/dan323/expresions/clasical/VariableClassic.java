package com.dan323.expresions.clasical;

import com.dan323.expresions.exceptions.InvalidMapValuesException;
import com.dan323.expresions.util.Variable;

import java.util.Map;

public final class VariableClassic implements ClassicalLogicOperation, Variable {

    private final String var;

    public VariableClassic(String var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public boolean equals(Object log) {
        return (log instanceof VariableClassic) && ((VariableClassic) log).var.equals(var);
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

    public boolean evaluate(Map<String, Boolean> values) {
        if (values.containsKey(var)) {
            return values.get(var);
        }
        throw new InvalidMapValuesException("The map " + values + " gives no value to " + var);
    }

}
