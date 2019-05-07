package com.dan323.expresions.util;

import com.dan323.expresions.LogicOperation;

/**
 * @author danco
 */
public interface Negation extends UnaryOperation{

    Negation construct(LogicOperation element);
}
