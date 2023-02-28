package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
public class LogicOperationStub implements LogicOperation {

    private final int ordinal;

    public LogicOperationStub(int x) {
        ordinal = x;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LogicOperationStub && ((LogicOperationStub) obj).ordinal == ordinal);
    }

    @Override
    public int hashCode() {
        return ordinal + LogicOperationStub.class.hashCode();
    }

    @Override
    public String toString() {
        return "LogicOperationBasic " + ordinal;
    }
}
