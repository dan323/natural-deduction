package com.dan323.expresions.base;

/**
 * @author danco
 */
public interface Constant<T extends LogicOperation> extends LogicOperation {

    boolean isFalsehood();

    default T castToLanguage() {
        return (T) this;
    }

}
