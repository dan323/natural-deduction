package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
public class BinaryOperationStub extends BinaryOperation<LogicOperation> {

    public BinaryOperationStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }

    @Override
    protected String getOperator() {
        return "op";
    }
}
