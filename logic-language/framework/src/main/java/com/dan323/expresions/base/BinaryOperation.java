package com.dan323.expresions.base;

/**
 * Any logic expression defined by applying binary operation between
 * @author danco
 */
public interface BinaryOperation extends LogicOperation {

    LogicOperation getLeft();
    LogicOperation getRight();

}
