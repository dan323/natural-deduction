package com.dan323.expressions.base.stub;

import com.dan323.expressions.base.Conjunction;
import com.dan323.expressions.base.LogicOperation;

/**
 * @author danco
 */
public class ConjuntionStub extends Conjunction<LogicOperation> {

    public ConjuntionStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
