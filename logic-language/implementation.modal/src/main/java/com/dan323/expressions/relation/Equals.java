package com.dan323.expressions.relation;

import java.util.Objects;

public final class Equals<T> extends RelationOperation<T> {

    public Equals(T left, T right) {
        super(left, right, "=");
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Equals<?> eq) {
            return eq.getLeft().equals(getLeft()) && eq.getRight().equals(getRight())
                    || eq.getRight().equals(getLeft()) && eq.getLeft().equals(getRight());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight(), getClass()) + Objects.hash(getRight(), getLeft(), getClass());
    }
}
