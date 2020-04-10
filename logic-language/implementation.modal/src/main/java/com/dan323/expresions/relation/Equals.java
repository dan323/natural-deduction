package com.dan323.expresions.relation;

import java.util.Objects;

public final class Equals<T> extends RelationOperation<T> {

    public Equals(T left, T right) {
        super(left, right, "=");
    }

    @Override
    public boolean equals(Object object) {
        return object != null
                && object.getClass().equals(getClass())
                && ((getLeft().equals(((RelationOperation<?>) object).getLeft())
                && getRight().equals(((RelationOperation<?>) object).getRight()))
                || (getLeft().equals(((RelationOperation<?>) object).getRight())
                && getRight().equals(((RelationOperation<?>) object).getLeft())));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight(), getClass()) + Objects.hash(getRight(), getLeft(), getClass());
    }
}
