package com.dan323.expresions.modal;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Negation;

import java.util.List;

public final class NegationModal extends UnaryOperationModal implements Negation {

    public NegationModal(ModalLogicalExpression elem){
        super(elem);
    }

    @Override
    protected String getOperator() {
        return "-";
    }

    @Override
    public Negation construct(LogicOperation element) {
        if (ModalLogicalExpression.isModal(element)){
            return LogicOperation.construct(NegationModal::construct,element);
        }
        throw new IllegalArgumentException();
    }

    private static NegationModal construct(List<LogicOperation> lst) {
        return new NegationModal((ModalLogicalExpression)lst.get(0));
    }
}
