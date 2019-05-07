package com.dan323.expresions.util;

import com.dan323.expresions.LogicOperation;

/**
 * @author danco
 */
public interface Disjunction extends BinaryOperation {

    Disjunction construct(LogicOperation left,LogicOperation right);
}
