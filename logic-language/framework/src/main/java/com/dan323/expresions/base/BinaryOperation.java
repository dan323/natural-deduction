package com.dan323.expresions.base;

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

    public T getLeft() {
        return left;
    }

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

    @Override
    public boolean equals(Object log) {
        if (log instanceof BinaryOperation && getOperator().equals(((BinaryOperation<?>) log).getOperator())) {
            return getLeft().equals(((BinaryOperation<?>) log).getLeft()) && getRight().equals(((BinaryOperation<?>) log).getRight());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return left.hashCode() * 17 + right.hashCode() * 13 + getOperator().hashCode() * 11;
    }


    protected abstract String getOperator();

}
