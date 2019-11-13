package com.dan323.expresions.base;

/**
 * @author danco
 */
public abstract class Constant<T extends LogicOperation> implements LogicOperation {

    public abstract boolean isFalsehood();

    public T castToLanguage(){
        return (T)this;
    }

}
