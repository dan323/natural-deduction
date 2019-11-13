package com.dan323.expresions.base.stub;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.UnaryOperation;

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
