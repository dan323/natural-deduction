package com.dan323.proof.generic.bean;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ToGoalParser<B extends ToGoalParser.GoalSupplier<T>, T, G> extends Function<B,G>, LogicAware {

    static <B extends GoalSupplier<T>,G,T> ToGoalParser<B,T,G> from(Supplier<String> ofLogic, Function<B,G> toGoal){
        return new ToGoalParser<>() {
            @Override
            public String ofLogic() {
                return ofLogic.get();
            }

            @Override
            public G apply(B b) {
                return toGoal.apply(b);
            }
        };
    }

    interface GoalSupplier<T>{
        String expression();
        T extraInformation();
    }
}
