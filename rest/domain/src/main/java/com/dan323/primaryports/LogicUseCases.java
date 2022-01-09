package com.dan323.primaryports;

import com.dan323.exception.InvalidProofException;
import com.dan323.exception.InvalidRuleApplicationException;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.ProofStep;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public interface LogicUseCases{

    GetAllActions getAllActions(String logic);
    <Q extends Serializable, C extends Action<L,S,P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L,A,G,S>> SaveRule saveRule(String logic, Proof<P, A, G, S, Q, L> proof);
    <Q extends Serializable, C extends Action<L,S,P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L,A,G,S>> ApplyRule<Q,S,L,A,G,P> applyRule(String logic, Proof<P, A, G, S, Q, L> proof, String ruleName, Input<Q> inputs);
    <Q extends Serializable, C extends Action<L,S,P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L,A,G,S>> IsDone isDone(String logic, Proof<P, A, G, S, Q, L> proof);

    @FunctionalInterface
    interface GetAllActions{
        Flux<String> perform();
    }

    @FunctionalInterface
    interface IsDone{
        Mono<Boolean> perform() throws InvalidProofException;
    }

    @FunctionalInterface
    interface ApplyRule<Q extends Serializable, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L,A,G,S>>{
        Mono<Proof<P, A, G, S, Q, L>> perform() throws InvalidProofException, InvalidRuleApplicationException;
    }

    @FunctionalInterface
    interface SaveRule{
        Mono<Void> perform() throws InvalidProofException;
    }

}
