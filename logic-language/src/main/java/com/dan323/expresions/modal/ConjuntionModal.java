package com.dan323.expresions.modal;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Conjuntion;

import java.util.List;

public final class ConjuntionModal extends BinaryOperationModal implements Conjuntion {

    public ConjuntionModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    @Override
    protected String getOperator() {
        return "&";
    }

    private static ConjuntionModal construct(List<LogicOperation> lst){
        return new ConjuntionModal((ModalLogicalExpression)lst.get(0),(ModalLogicalExpression)lst.get(1));
    }

    @Override
    public Conjuntion construct(LogicOperation left, LogicOperation right) {
        if (ModalLogicalExpression.areModal(left,right)){
            return LogicOperation.construct(ConjuntionModal::construct,left,right);
        }
        throw new IllegalArgumentException();
    }
}
