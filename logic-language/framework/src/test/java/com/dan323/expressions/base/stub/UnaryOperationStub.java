package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.UnaryOperation;

/**
 * @author danco
 */
public class UnaryOperationStub extends UnaryOperation<LogicOperation> {

    public UnaryOperationStub(LogicOperation element) {
        super(element);
    }

    @Override
    protected String getOperator() {
        return "unop";
    }
}
