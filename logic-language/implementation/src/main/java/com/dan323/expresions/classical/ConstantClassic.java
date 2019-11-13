package com.dan323.expresions.classical;

import com.dan323.expresions.base.Constant;

import java.util.Map;

public class ConstantClassic extends Constant<ClassicalLogicOperation> implements ClassicalLogicOperation {

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
    public boolean evaluate(Map<String, Boolean> values) {
        return getValue();
    }

    @Override
    public String toString() {
        return String.valueOf(val).toUpperCase();
    }
}
