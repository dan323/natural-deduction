package com.dan323.expressions.relation;

import java.util.Objects;

public final class LessEqual extends RelationOperation {

    public LessEqual(String left, String right) {
        super(left, right, "<=");
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof LessEqual lessEqual) {
            return lessEqual.getLeft().equals(getLeft()) && lessEqual.getRight().equals(getRight());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight(), getClass());
    }

}
