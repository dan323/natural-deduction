package com.dan323.expresions.clasical;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Conjuntion;

import java.util.List;
import java.util.Map;

public final class ConjuntionClassic extends BinaryOperationClassic implements Conjuntion {

    public ConjuntionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "&";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) && getRight().evaluate(values);
    }

    private static ConjuntionClassic construct(List<LogicOperation> lst){
        return new ConjuntionClassic((ClassicalLogicOperation)lst.get(0),(ClassicalLogicOperation)lst.get(1));
    }

    @Override
    public Conjuntion construct(LogicOperation left, LogicOperation right) {
        if (ClassicalLogicOperation.areClassical(left,right)){
            LogicOperation.construct(ConjuntionClassic::construct,left,right);
        }
        throw new IllegalArgumentException();
    }
}
