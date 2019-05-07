package com.dan323.expresions.util;

import com.dan323.expresions.LogicOperation;

/**
 * @author danco
 */
public interface Implication extends BinaryOperation {

    Implication construct(LogicOperation left,LogicOperation right);

}
