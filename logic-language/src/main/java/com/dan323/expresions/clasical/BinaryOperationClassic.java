package com.dan323.expresions.clasical;

import com.dan323.expresions.util.BinaryOperation;

public abstract class BinaryOperationClassic implements ClassicalLogicOperation, BinaryOperation {

    private final ClassicalLogicOperation left;
    private final ClassicalLogicOperation right;

    public BinaryOperationClassic(ClassicalLogicOperation l, ClassicalLogicOperation r){
        left=l;
        right=r;
    }

    @Override
    public boolean equals(Object log) {
        if (log instanceof BinaryOperationClassic && getOperator().equals(((BinaryOperationClassic) log).getOperator())){
            return left.equals(((BinaryOperationClassic) log).left) && right.equals(((BinaryOperationClassic) log).right);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return left.hashCode()*17+right.hashCode();
    }

    public ClassicalLogicOperation getLeft(){
        return  left;
    }

    public ClassicalLogicOperation getRight(){
        return right;
    }

    @Override
    public String toString(){
        String leftS=left.toString();
        if (left instanceof BinaryOperationClassic || left instanceof UnaryOperationClassic){
            leftS="("+leftS+")";
        }
        String rightS=right.toString();
        if (right instanceof BinaryOperationClassic || right instanceof UnaryOperationClassic){
            rightS="("+rightS+")";
        }
        return leftS+" "+ getOperator() +" "+rightS;
    }

    protected abstract String getOperator();
}
