package com.dan323.expressions.relation;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.modal.ModalOperation;

public abstract class RelationOperation<T> implements ModalOperation {
    private final T left;
    private final T right;
    private final String operation;

    public RelationOperation(T left, T right, String operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public T getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    public String toString() {
        return left.toString() + " " + operation + " " + right.toString();
    }

    public static boolean areRelation(LogicOperation... logs) {
        for (LogicOperation log : logs) {
            if (!isRelation(log)) {
                return false;
            }
        }
        return true;
    }

    static boolean isRelation(LogicOperation log) {
        return log instanceof RelationOperation;
    }
}
