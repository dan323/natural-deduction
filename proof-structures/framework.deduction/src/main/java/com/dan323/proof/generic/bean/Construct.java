package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.function.BiFunction;

public interface Construct<K extends Input<?>, L extends LogicOperation, A extends Action<?, ?, ?>> {

    int getInputs();

    boolean isNeedsExpr();

    boolean isNeedsExtra();

    String getName();

    String name();

    BiFunction<K,ExpressionParser<L>, A> getCons();

}
