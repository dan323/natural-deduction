package com.dan323.expressions.relation;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.modal.ModalOperation;

public abstract class RelationOperation implements ModalOperation {
    private final String left;
    private final String right;
    private final String operation;

    public RelationOperation(String left, String right, String operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String toString() {
        return left + " " + operation + " " + right;
    }

    static boolean isRelation(LogicOperation log) {
        return log instanceof RelationOperation;
    }
}
