package com.dan323.expresions.base;

/**
 * @author danco
 */
public interface Constant<T extends LogicOperation> extends LogicOperation {

    public abstract boolean isFalsehood();

    default T castToLanguage() {
        return (T) this;
    }

}
