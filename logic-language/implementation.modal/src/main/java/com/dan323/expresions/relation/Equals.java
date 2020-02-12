package com.dan323.expresions.relation;

public final class Equals<T> extends RelationOperation<T> {

    public Equals(T left, T right) {
        super(left, right, "=");
    }

}
