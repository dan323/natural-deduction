package com.dan323.expresions.classical;

import com.dan323.expresions.base.Constant;

import java.util.Map;

public final class ConstantClassic implements ClassicalLogicOperation, Constant {

    public static final ConstantClassic FALSE = new ConstantClassic(false);
    public static final ConstantClassic TRUE = new ConstantClassic(true);
    private final boolean val;

    private ConstantClassic(boolean b) {
        val = b;
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }

    @Override
    public boolean equals(Object log) {
        return (log instanceof ConstantClassic) && ((ConstantClassic) log).getValue() == val;
    }

    @Override
    public int hashCode() {
        return val ? 1 : 0;
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getValue();
    }

    @Override
    public Constant construct(int val) {
        if (val != 0 && val != 1) {
            throw new IllegalArgumentException();
        }
        return new ConstantClassic(val == 1);
    }
}
