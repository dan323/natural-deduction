package com.dan323.expressions.classical;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.classical.exceptions.InvalidMapValuesException;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A classical logical expression can be evaluated when all propositional variables are given truth value
 */
public interface ClassicalLogicOperation extends LogicOperation {

    static boolean isClassical(LogicOperation log) {
        return log instanceof ClassicalLogicOperation;
    }

    static boolean classicalOperationEquals(Object obj, Predicate<Object> equalsMethod) {
        return LogicOperation.logicOperationEquals(obj, ClassicalLogicOperation::isClassical, equalsMethod);
    }

    static boolean areClassical(LogicOperation... logs) {
        for (LogicOperation log : logs) {
            if (!isClassical(log)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Truth value of the expression
     *
     * @param vals values for the propositional variables
     * @return evaluation of the expression
     * @throws InvalidMapValuesException if not all variables have value
     */
    boolean evaluate(Map<String, Boolean> vals);
}
