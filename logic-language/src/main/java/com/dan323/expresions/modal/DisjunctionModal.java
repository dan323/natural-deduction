package com.dan323.expresions.modal;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Disjunction;

import java.util.List;

public final class DisjunctionModal extends BinaryOperationModal implements Disjunction {

    public DisjunctionModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "|";
    }

    @Override
    public Disjunction construct(LogicOperation left, LogicOperation right) {
        if (ModalLogicalExpression.areModal(left,right)){
            LogicOperation.construct(DisjunctionModal::construct,left,right);
        }
        throw new IllegalArgumentException();
    }

    private static DisjunctionModal construct(List<LogicOperation> lst) {
        return new DisjunctionModal((ModalLogicalExpression)lst.get(0),(ModalLogicalExpression)lst.get(1));
    }
}