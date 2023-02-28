package com.dan323.expressions.classical;

import com.dan323.expressions.base.Constant;

import java.util.Map;

public enum ConstantClassic implements ClassicalLogicOperation, Constant {

    FALSE(false),TRUE(true);

    private final boolean val;

    ConstantClassic(boolean b) {
        val = b;
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public boolean isFalsehood() {
        return equals(FALSE);
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getValue();
    }

    @Override
    public String toString() {
        return String.valueOf(val).toUpperCase();
    }
}
