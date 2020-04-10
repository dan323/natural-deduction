package com.dan323.expresions.relation;

import java.util.Objects;

public final class LessEqual<T> extends RelationOperation<T> {

    public LessEqual(T left, T right) {
        super(left, right, "<=");
    }

    @Override
    public boolean equals(Object object) {
        return object != null
                && object.getClass().equals(getClass())
                && getLeft().equals(((RelationOperation<?>) object).getLeft())
                && getRight().equals(((RelationOperation<?>) object).getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight(), getClass());
    }

}
