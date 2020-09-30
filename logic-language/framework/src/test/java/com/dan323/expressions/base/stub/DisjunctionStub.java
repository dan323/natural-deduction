package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.Disjunction;
import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
public class DisjunctionStub extends Disjunction<LogicOperation> {
    public DisjunctionStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
