package com.dan323.proof.generic.bean;

import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public interface ParseAction<A extends Action<?, ?, P>, P extends Proof<?,?,?,?>> {

    String ofLogic();

    A parse(P proof, int pos);

    default List<A> translateToActions(P proof) {
        List<A> lists = new ArrayList<>();
        IntStream.range(0, proof.getSteps().size()).forEach(i ->
                lists.add(parse(proof, i + 1)));
        return lists;
    }

    static <A extends Action<?,?,P>, P extends Proof<?,?,?,?>> ParseAction<A , P> toParseAction(Supplier<String> ofLogic, BiFunction<P,Integer,A> biFunction){
        return new ParseAction<>() {
            @Override
            public String ofLogic() {
                return ofLogic.get();
            }

            @Override
            public A parse(P proof, int pos) {
                return biFunction.apply(proof, pos);
            }
        };
    }

}
