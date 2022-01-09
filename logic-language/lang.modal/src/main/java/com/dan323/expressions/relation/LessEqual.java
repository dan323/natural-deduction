package com.dan323.expressions.relation;

import java.util.Objects;

public final class LessEqual<T> extends RelationOperation<T> {

    public LessEqual(T left, T right) {
        super(left, right, "<=");
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof LessEqual le)
                && getLeft().equals(le.getLeft())
                && getRight().equals(le.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight(), getClass());
    }

}
