package com.dan323.proof.generic.proof;

import com.dan323.proof.generic.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public interface ParseAction<A extends Action<?, ?, P>, P extends Proof<?, ?,A>> {

    A parse(P proof, int pos);

    default List<A> translateToActions(P proof) {
        List<A> lists = new ArrayList<>();
        IntStream.range(0, proof.getSteps().size()).forEach(i ->
                lists.add(parse(proof, i + 1)));
        return lists;
    }

}
