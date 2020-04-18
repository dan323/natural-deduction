package com.dan323.expresions.base;

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

    @Override
    public boolean equals(Object log) {
        return log instanceof UnaryOperation && getOperator().equals(((UnaryOperation<T>) log).getOperator()) && getElement().equals(((UnaryOperation<T>) log).getElement());
    }

    @Override
    public int hashCode() {
        return getElement().hashCode() * 13 + getOperator().hashCode() * 11;
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
