package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.Step;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ToModel<T, S extends ProofStep<L>, L extends LogicOperation> extends LogicAware, Function<Step<T>,S>{

    Step<T> applyInv(S step);

    static <T, S extends ProofStep<L>, L extends LogicOperation> ToModel<T,S,L> from(Supplier<String> ofLogic, Function<Step<T>,S> toModel, Function<S,Step<T>> toModelInv){
        return new ToModel<>() {

            @Override
            public String ofLogic() {
                return ofLogic.get();
            }

            @Override
            public S apply(Step<T> tStep) {
                return toModel.apply(tStep);
            }

            @Override
            public Step<T> applyInv(S tStep) {
                return toModelInv.apply(tStep);
            }
        };
    }
}
