package com.dan323.expresions.modal;

import com.dan323.expresions.util.UnaryOperation;

public abstract class UnaryOperationModal implements ModalLogicalExpression, UnaryOperation {

    private final ModalLogicalExpression element;

    public UnaryOperationModal(ModalLogicalExpression element){
        this.element=element;
    }

    public ModalLogicalExpression getElement(){
        return element;
    }


    public String toString(){
        return getOperator() +" ("+getElement()+")";
    }

    protected abstract String getOperator();

    @Override
    public boolean equals(Object log) {
        return log instanceof UnaryOperationModal && getOperator().equals(((UnaryOperationModal) log).getOperator()) && element.equals(((UnaryOperationModal) log).element);
    }

    @Override
    public int hashCode(){
        return super.hashCode()*element.hashCode();
    }
}
