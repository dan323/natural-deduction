package com.dan323.expressions.base;

/**
 * Any logic expression defined by applying binary operation between
 *
 * @author danco
 */
public abstract class BinaryOperation<T extends LogicOperation> implements LogicOperation {

    private final T left;
    private final T right;

    public BinaryOperation(T left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Left formula
     * @return formula A such that this is BinaryOperation(A,B)
     */
    public T getLeft() {
        return left;
    }

    /**
     * Right formula
     * @return formula A such that this is BinaryOperation(B,A)
     */
    public T getRight() {
        return right;
    }

    public String toString() {
        String leftS = getLeft().toString();
        if (getLeft() instanceof BinaryOperation || getLeft() instanceof UnaryOperation) {
            leftS = "(" + leftS + ")";
        }
        String rightS = getRight().toString();
        if (getRight() instanceof BinaryOperation || getRight() instanceof UnaryOperation) {
            rightS = "(" + rightS + ")";
        }
        return leftS + " " + getOperator() + " " + rightS;
    }

    protected abstract String getOperator();

}
