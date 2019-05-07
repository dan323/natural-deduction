package com.dan323.expresions.clasical;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Negation;

import java.util.Map;

public final class NegationClassic extends UnaryOperationClassic implements Negation {

    public NegationClassic(ClassicalLogicOperation elem){
        super(elem);
    }

    @Override
    protected String getOperator() {
        return "-";
    }

    @Override
    public boolean evaluate(Map<String, Boolean> values){
        return !getElement().evaluate(values);
    }

    @Override
    public Negation construct(LogicOperation element) {
        if (element instanceof ClassicalLogicOperation){
            return new NegationClassic((ClassicalLogicOperation)element);
        }
        throw new IllegalArgumentException();
    }
}
