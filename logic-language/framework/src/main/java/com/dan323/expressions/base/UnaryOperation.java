package com.dan323.expressions.base;

/**
 * @author danco
 */
public abstract class UnaryOperation<T extends LogicOperation> implements LogicOperation {

    private final T element;

    public UnaryOperation(T element) {
        this.element = element;
    }

    public T castToLanguage() {
        return (T) this;
    }

    public T getElement() {
        return element;
    }

    public String toString() {
        String leftS = getElement().toString();
        if (getElement() instanceof BinaryOperation || getElement() instanceof UnaryOperation) {
            leftS = "(" + leftS + ")";
        }
        return getOperator() + " " + leftS;
    }

    protected abstract String getOperator();
}
