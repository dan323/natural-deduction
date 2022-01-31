package com.dan323;

import com.dan323.exception.FailedToSaveRuleException;
import com.dan323.exception.InvalidProofException;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.*;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.secondaryports.RuleDao;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveRuleUseCase<Q extends Serializable, C extends Action<L, S, P>, P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, L extends LogicOperation, S extends ProofStep<L>> implements LogicUseCases.SaveRule {

    private final String logic;
    private final Proof<P, A, G, S, Q, L> proof;
    private final RuleDao ruleDao;
    private final ParseAction<C, P> parseAction;
    private final NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory;
    private final ToGoalParser<Goal<Q>, Q, G> toGoalParser;
    private final ToAssmParser<Assumption<Q>, Q, A> toAssmParser;
    private final ToModel<Q, S, L> toModel;

    public SaveRuleUseCase(String logic, Proof<P, A, G, S, Q, L> proof,
                           RuleDao ruleDao,
                           ParseAction<C, P> parseAction,
                           NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory,
                           ToGoalParser<Goal<Q>, Q, G> toGoalParser,
                           ToAssmParser<Assumption<Q>, Q, A> toAssmParser,
                           ToModel<Q, S, L> toModel) {
        this.logic = logic;
        this.proof = proof;
        this.ruleDao = ruleDao;
        this.parseAction = parseAction;
        this.naturalDeductionFactory = naturalDeductionFactory;
        this.toGoalParser = toGoalParser;
        this.toAssmParser = toAssmParser;
        this.toModel = toModel;
    }

    @Override
    public Mono<Void> perform() throws InvalidProofException {
        if (validate()) {
            return save().flatMap(bool -> {
                if (bool) {
                    return Mono.empty();
                } else {
                    return Mono.error(new FailedToSaveRuleException());
                }
            });
        } else {
            throw new InvalidProofException();
        }
    }

    private Mono<Boolean> save() {
        return ruleDao.saveRule(logic, new Rule<>(
                extractPremises()
                        .collect(Collectors.toList()),
                proof.goal(),
                proof.name(),
                proof.name(),
                0,
                false,
                false));
    }

    private boolean validate() {
        var actionList = parseAction.translateToActions(proof.toModelProof(naturalDeductionFactory, toAssmParser, toGoalParser, toModel));
        var count = extractPremises().count();
        for (int i = 0; i < count; i++) {
            actionList.remove(i);
        }
        var pr = proof.toModelInitialProof(naturalDeductionFactory, toAssmParser, toGoalParser);
        try {
            actionList.forEach(act -> {
                if (act.isValid(pr)){
                    act.apply(pr);
                }
                else {
                    throw new IllegalArgumentException();
                }
            });
        } catch (IllegalArgumentException e){
            return false;
        }
        return pr.isDone();
    }

    private Stream<Assumption<Q>> extractPremises() {
        return proof.steps()
                .stream()
                .takeWhile(qStep -> qStep.assmsLevel() == 0)
                .takeWhile(qStep -> qStep.ruleString().name().contains("Ass"))
                .map(step -> new Assumption<>(step.expression(), step.extraInformation()));
    }
}
