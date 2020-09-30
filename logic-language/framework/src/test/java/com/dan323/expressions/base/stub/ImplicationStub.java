package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.Implication;
import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
public class ImplicationStub extends Implication<LogicOperation> {
    public ImplicationStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
