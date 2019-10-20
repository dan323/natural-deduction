package com.dan323.expresions.modal;

import com.dan323.expresions.base.BinaryOperation;

public abstract class BinaryOperationModal implements ModalLogicalExpression, BinaryOperation {

    private final ModalLogicalExpression left;
    private final ModalLogicalExpression right;

    public BinaryOperationModal(ModalLogicalExpression l, ModalLogicalExpression r){
        left=l;
        right=r;
    }

    @Override
    public final boolean equals(Object log) {
        return log instanceof BinaryOperationModal && // The object is a binary operation
                getOperator().equals(((BinaryOperationModal) log).getOperator()) && // The operator is the same
                left.equals(((BinaryOperationModal) log).left) && // The left operands are equal
                right.equals(((BinaryOperationModal) log).right); // The right operands are equal
    }

    @Override
    public final int hashCode(){
        return left.hashCode()*12+right.hashCode()*super.hashCode()*getOperator().hashCode();
    }

    public ModalLogicalExpression getLeft(){
        return  left;
    }

    public ModalLogicalExpression getRight(){
        return right;
    }

    @Override
    public final String toString(){
        return "("+left.toString()+") "+ getOperator() +" ("+right.toString()+")";
    }

    protected abstract String getOperator();
}
