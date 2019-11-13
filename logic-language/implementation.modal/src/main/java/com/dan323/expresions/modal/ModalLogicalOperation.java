package com.dan323.expresions.modal;

import com.dan323.expresions.base.LogicOperation;

import java.util.function.Predicate;

public interface ModalLogicalOperation extends LogicOperation {

    static boolean areModal(LogicOperation... logs) {
        for (LogicOperation log : logs) {
            if (!isModal(log)) {
                return false;
            }
        }
        return true;
    }

    static boolean modalOperationEquals(Object obj, Predicate<Object> equalsMethod) {
        return LogicOperation.logicOperationEquals(obj, ModalLogicalOperation::isModal, equalsMethod);
    }

    static boolean isModal(LogicOperation log) {
        return log instanceof ModalLogicalOperation;
    }
}
