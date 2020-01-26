package com.dan323.expresions.base.stub;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;

public class ConstantStub implements Constant<LogicOperation> {

    @Override
    public boolean isFalsehood() {
        return false;
    }
}
