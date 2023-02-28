package com.dan323.expressions.classical;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.classical.exceptions.InvalidMapValuesException;

import java.util.Map;

/**
 * A classical logical expression can be evaluated when all propositional variables are given truth value
 */
public interface ClassicalLogicOperation extends LogicOperation {

    /**
     * Truth value of the expression given variable values
     *
     * @param vals values for the propositional variables
     * @return evaluation of the expression
     * @throws InvalidMapValuesException if not all variables have value
     */
    boolean evaluate(Map<String, Boolean> vals);
}
