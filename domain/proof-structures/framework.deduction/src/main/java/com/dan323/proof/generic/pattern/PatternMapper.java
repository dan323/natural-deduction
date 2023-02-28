package com.dan323.proof.generic.pattern;

import com.dan323.expressions.base.LogicOperation;

import java.util.Map;

@FunctionalInterface
public interface PatternMapper<T extends LogicOperation> {

    Map<String, T> compareLogic(T l1, T l2);

}
