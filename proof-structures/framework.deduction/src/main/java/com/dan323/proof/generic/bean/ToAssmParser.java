package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;

import java.util.function.Function;
import java.util.function.Supplier;


public interface ToAssmParser<B extends ToAssmParser.AssumptionSupplier<T>, T, A> extends LogicAware, Function<B,A> {

    static <B extends ToAssmParser.AssumptionSupplier<T>,A, T> ToAssmParser<B,T,A> from(Supplier<String> ofLogic, Function<B,A> parser){
        return new ToAssmParser<>() {
            @Override
            public String ofLogic() {
                return ofLogic.get();
            }

            @Override
            public A apply(B b) {
                return parser.apply(b);
            }
        };
    }

    interface AssumptionSupplier<T> {
        String expression();

        T extraInformation();
    }

}
