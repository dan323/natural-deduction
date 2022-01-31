package com.dan323;

import com.dan323.exception.InvalidProofException;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.Assumption;
import com.dan323.primaryports.Goal;
import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofStep;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.stream.Stream;

public class IsDoneUseCase<Q extends Serializable, C extends Action<L, S, P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>> implements LogicUseCases.IsDone {

    private final Proof<P, A, G, S, Q, L> proof;
    private final NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory;
    private final ToAssmParser<Assumption<Q>, Q, A> toAssmParser;
    private final ToGoalParser<Goal<Q>, Q, G> toGoalParser;
    private final ToModel<Q, S, L> toModel;
    private final ParseAction<C, P> parseAction;

    public IsDoneUseCase(Proof<P, A, G, S, Q, L> proof,
                         NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory,
                         ToAssmParser<Assumption<Q>, Q, A> toAssmParser,
                         ToGoalParser<Goal<Q>, Q, G> toGoalParser,
                         ToModel<Q, S, L> toModel,
                         ParseAction<C, P> parseAction) {
        this.proof = proof;
        this.naturalDeductionFactory = naturalDeductionFactory;
        this.toAssmParser = toAssmParser;
        this.toGoalParser = toGoalParser;
        this.toModel = toModel;
        this.parseAction = parseAction;
    }

    @Override
    public Mono<Boolean> perform() {
        return Mono.just(proof)
                .map(pr -> pr.toModelProof(naturalDeductionFactory, toAssmParser, toGoalParser, toModel))
                .flatMap(this::validate)
                .map(com.dan323.proof.generic.proof.Proof::isDone);
    }

    private Stream<Assumption<Q>> extractPremises() {
        return proof.steps()
                .stream()
                .takeWhile(qStep -> qStep.assmsLevel() == 0)
                .takeWhile(qStep -> qStep.ruleString().name().contains("Ass"))
                .map(step -> new Assumption<>(step.expression(), step.extraInformation()));
    }

    private Mono<P> validate(P p) {
        var actionList = parseAction.translateToActions(p);
        var count = extractPremises().count();
        for (int i = 0; i < count; i++) {
            actionList.remove(i);
        }
        var pr = proof.toModelInitialProof(naturalDeductionFactory, toAssmParser, toGoalParser);
        try {
            actionList.forEach(act -> {
                if (act.isValid(pr)){
                    act.apply(pr);
                } else {
                    throw new IllegalArgumentException();
                }
            });
        } catch (IllegalArgumentException e){
            return Mono.error(new InvalidProofException());
        }
        return Mono.just(p);
    }
}
