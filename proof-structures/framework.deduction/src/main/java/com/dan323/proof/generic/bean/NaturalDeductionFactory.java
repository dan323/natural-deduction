package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.function.Supplier;

public interface NaturalDeductionFactory<T extends Proof<A,B>, A extends LogicOperation, B extends ProofStep<A>> extends LogicAware, Supplier<T> {

    static <T extends Proof<A,B>, A extends LogicOperation, B extends ProofStep<A>> NaturalDeductionFactory<T,A,B> of(Supplier<String> logic, Supplier<T> proof){
        return new NaturalDeductionFactory<>() {
            @Override
            public String ofLogic() {
                return logic.get();
            }

            @Override
            public T get() {
                return proof.get();
            }
        };
    }

}
