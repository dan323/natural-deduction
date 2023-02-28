package com.dan323.expressions.base;

/**
 * @author danco
 */
public interface Constant extends LogicOperation {

    /**
     * @return true if this constant represents {@code False}
     */
    boolean isFalsehood();

}
