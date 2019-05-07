package com.dan323.expresions.util;

import com.dan323.expresions.LogicOperation;

/**
 * @author danco
 */
public interface Conjuntion extends BinaryOperation {

    Conjuntion construct(LogicOperation left,LogicOperation right);
}
