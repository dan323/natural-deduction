package com.dan323.proof;


import com.dan323.expresions.LogicOperation;

import java.util.Map;

@FunctionalInterface
public interface PatternMapper<T extends LogicOperation> {

    Map<String, T> compareLogic(T l1, T l2);

}
