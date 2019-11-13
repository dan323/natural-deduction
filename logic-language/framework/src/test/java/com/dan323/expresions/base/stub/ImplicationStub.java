package com.dan323.expresions.base.stub;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;

/**
 * @author danco
 */
public class ImplicationStub extends Implication<LogicOperation> {
    public ImplicationStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
