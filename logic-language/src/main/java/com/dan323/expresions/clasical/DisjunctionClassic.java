package com.dan323.expresions.clasical;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Disjunction;

import java.util.List;
import java.util.Map;

public final class DisjunctionClassic extends BinaryOperationClassic implements Disjunction {

    public DisjunctionClassic(ClassicalLogicOperation l, ClassicalLogicOperation r) {
        super(l, r);
    }

    private static DisjunctionClassic construct(List<LogicOperation> lst) {
        return new DisjunctionClassic((ClassicalLogicOperation) lst.get(0), (ClassicalLogicOperation) lst.get(1));
    }

    @Override
    protected String getOperator() {
        return "|";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values) {
        return getLeft().evaluate(values) || getRight().evaluate(values);
    }

    @Override
    public Disjunction construct(LogicOperation left, LogicOperation right) {
        if (ClassicalLogicOperation.areClassical(left, right)) {
            return LogicOperation.construct(DisjunctionClassic::construct, left, right);
        }
        throw new IllegalArgumentException();
    }
}