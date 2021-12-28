package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ExpressionParser<T extends LogicOperation> extends Function<String,T>, LogicAware {

    public static <T extends LogicOperation> ExpressionParser<T> of(Function<String,T> parser, Supplier<String> logic){
        return new ExpressionParser<T>() {

            @Override
            public String ofLogic() {
                return logic.get();
            }

            @Override
            public T apply(String s) {
                return parser.apply(s);
            }
        };
    }
}
