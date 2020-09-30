package com.dan323.expressions.base;

import java.util.function.Predicate;

/**
 * Interface to abstract all logic expressions
 */
public interface LogicOperation {

    static boolean logicOperationEquals(Object object, Predicate<LogicOperation> check, Predicate<Object> equalsFunction) {
        if (object instanceof LogicOperation && check.test((LogicOperation)object)) {
            return equalsFunction.test(object);
        } else {
            return false;
        }
    }

}
