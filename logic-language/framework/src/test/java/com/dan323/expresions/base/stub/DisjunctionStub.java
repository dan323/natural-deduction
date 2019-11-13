package com.dan323.expresions.base.stub;

import com.dan323.expresions.base.Disjunction;
import com.dan323.expresions.base.LogicOperation;

/**
 * @author danco
 */
public class DisjunctionStub extends Disjunction<LogicOperation> {
    public DisjunctionStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
