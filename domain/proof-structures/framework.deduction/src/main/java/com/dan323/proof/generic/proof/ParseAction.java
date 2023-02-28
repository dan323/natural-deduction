package com.dan323.proof.generic.proof;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public interface ParseAction<A extends Action<T, Q, P>, P extends Proof<T, Q>, T extends LogicOperation, Q extends ProofStep<T>> {

    A parse(P proof, int pos);

    default List<A> translateToActions(P proof) {
        List<A> lists = new ArrayList<>();
        IntStream.range(0, proof.getSteps().size()).forEach(i ->
                lists.add(parse(proof, i + 1)));
        return lists;
    }

}
