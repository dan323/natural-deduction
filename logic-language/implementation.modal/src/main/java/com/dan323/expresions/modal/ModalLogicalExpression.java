package com.dan323.expresions.modal;

import com.dan323.expresions.base.LogicOperation;

public interface ModalLogicalExpression extends LogicOperation {

    static boolean areModal(LogicOperation ... logs) {
        for (LogicOperation log: logs) {
            if (!isModal(log)){
                return false;
            }
        }
        return true;
    }

    static boolean isModal(LogicOperation log){
        return log instanceof ModalLogicalExpression;
    }
}
