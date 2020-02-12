package com.dan323.expresions.relation;

public final class LessEqual<T> extends RelationOperation<T> {

    public LessEqual(T left, T right) {
        super(left, right, "<=");
    }

}
