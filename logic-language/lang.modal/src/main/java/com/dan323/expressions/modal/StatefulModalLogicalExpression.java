package com.dan323.expressions.modal;

public class StatefulModalLogicalExpression implements ModalOperation {

    private final String label;
    private final ModalLogicalOperation expression;

    public StatefulModalLogicalExpression(String label, ModalLogicalOperation expression){
        this.label = label;
        this.expression =expression;
    }

    public String getLabel() {
        return label;
    }

    public ModalLogicalOperation getExpression() {
        return expression;
    }
}
