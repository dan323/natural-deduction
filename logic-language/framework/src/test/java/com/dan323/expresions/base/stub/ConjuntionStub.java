package com.dan323.expresions.base.stub;

import com.dan323.expresions.base.Conjunction;
import com.dan323.expresions.base.LogicOperation;

/**
 * @author danco
 */
public class ConjuntionStub extends Conjunction<LogicOperation> {

    public ConjuntionStub(LogicOperation left, LogicOperation right) {
        super(left, right);
    }
}
