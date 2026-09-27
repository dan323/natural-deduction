package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.Constant;

import java.util.Set;

/**
 * The constants {@code TRUE} and {@code FALSE}, reserved words of the parser
 */
public enum ConstantFirstOrder implements FirstOrderOperation, Constant {

    FALSE, TRUE;

    @Override
    public boolean isFalsehood() {
        return this == FALSE;
    }

    @Override
    public Set<String> freeVariables() {
        return Set.of();
    }

    @Override
    public ConstantFirstOrder substitute(String variable, Term term) {
        return this;
    }
}
