package com.dan323.expresions.util;

import com.dan323.expresions.LogicOperation;

/**
 * @author danco
 */
public interface BinaryOperation extends LogicOperation {

    LogicOperation getLeft();
    LogicOperation getRight();

}
