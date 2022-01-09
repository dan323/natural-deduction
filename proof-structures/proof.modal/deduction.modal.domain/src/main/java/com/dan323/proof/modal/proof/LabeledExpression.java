package com.dan323.proof.modal.proof;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;

public class LabeledExpression<T> {

    private final T label;
    private final ModalOperation expression;

    public LabeledExpression(T label, ModalLogicalOperation expression){
        this.label = label;
        this.expression = expression;
    }

    public LabeledExpression(RelationOperation<T> expression){
        this.label = null;
        this.expression = expression;
    }

    public T getLabel() {
        return label;
    }

    public ModalOperation getExpression() {
        return expression;
    }
}
