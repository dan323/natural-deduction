package com.dan323.expresions.modal;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Constant;

public final class ConstantModal implements ModalLogicalExpression, Constant {

    private final boolean val;

    public boolean getValue(){
        return val;
    }

    public ConstantModal(boolean b){
        val=b;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }

    @Override
    public boolean equals(Object ltl) {
        return (ltl instanceof ConstantModal) && ((ConstantModal)ltl).val==val;
    }

    @Override
    public int hashCode(){
        return val?1:0;
    }

    @Override
    public Constant construct(int val) {
        if (val!=0 && val!=1){
            throw new IllegalArgumentException();
        }
        return LogicOperation.construct(lst->new ConstantModal(val==1));
    }
}
