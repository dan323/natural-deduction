package com.dan323.expresions.clasical;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.exceptions.InvalidMapValuesException;

import java.util.Map;

/**
 * A classical logical expression can be evaluated when all propositional variables are given truth value
 */
public interface ClassicalLogicOperation extends LogicOperation {

    static boolean isClassical(LogicOperation log) {
        return log instanceof ClassicalLogicOperation;
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
