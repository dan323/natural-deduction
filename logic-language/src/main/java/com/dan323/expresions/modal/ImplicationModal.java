package com.dan323.expresions.modal;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Implication;

import java.util.List;

public final class ImplicationModal extends BinaryOperationModal implements Implication {


    public ImplicationModal(ModalLogicalExpression l, ModalLogicalExpression r) {
        super(l, r);
    }

    private static ImplicationModal construct(List<LogicOperation> lst) {
        return new ImplicationModal((ModalLogicalExpression) lst.get(0), (ModalLogicalExpression) lst.get(1));
    }

    @Override
    protected String getOperator() {
        return "->";
    }

    @Override
    public Implication construct(LogicOperation left, LogicOperation right) {
        if (ModalLogicalExpression.areModal(left, right)) {
            return LogicOperation.construct(ImplicationModal::construct, left, right);
        }
        throw new IllegalArgumentException();
    }
}
