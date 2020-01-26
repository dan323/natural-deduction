package com.dan323.expresions.classical;

import com.dan323.expresions.base.Constant;

import java.util.Map;

public final class ConstantClassic implements ClassicalLogicOperation, Constant<ClassicalLogicOperation> {

    public final static ConstantClassic FALSE = new ConstantClassic(false);
    public final static ConstantClassic TRUE = new ConstantClassic(true);

    private final boolean val;

    private ConstantClassic(boolean b) {
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
    public int hashCode() {
        return super.hashCode() * 3 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstantClassic && ((ConstantClassic) obj).val == val;
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
