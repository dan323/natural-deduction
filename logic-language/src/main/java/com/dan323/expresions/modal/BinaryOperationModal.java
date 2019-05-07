package com.dan323.expresions.modal;

import com.dan323.expresions.util.BinaryOperation;

public abstract class BinaryOperationModal implements ModalLogicalExpression, BinaryOperation {

    private final ModalLogicalExpression left;
    private final ModalLogicalExpression right;

    public BinaryOperationModal(ModalLogicalExpression l, ModalLogicalExpression r){
        left=l;
        right=r;
    }

    @Override
    public boolean equals(Object log) {
        if (log instanceof BinaryOperationModal && getOperator().equals(((BinaryOperationModal) log).getOperator())){
            return left.equals(((BinaryOperationModal) log).left) && right.equals(((BinaryOperationModal) log).right);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return left.hashCode()*12+right.hashCode();
    }

    public ModalLogicalExpression getLeft(){
        return  left;
    }

    public ModalLogicalExpression getRight(){
        return right;
    }

    public String toString(){
        return "("+left.toString()+") "+ getOperator() +" ("+right.toString()+")";
    }

    protected abstract String getOperator();
}
