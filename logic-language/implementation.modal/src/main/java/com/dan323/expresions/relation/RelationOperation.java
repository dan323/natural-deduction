package com.dan323.expresions.relation;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.modal.ModalOperation;

import java.util.Objects;
import java.util.function.Predicate;

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

    static boolean areRelation(LogicOperation... logs) {
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
