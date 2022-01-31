package com.dan323;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.*;
import com.dan323.primaryports.Input;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.secondaryports.RuleDao;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.stream.Collectors;

public class ApplyRuleUseCase<Q extends Serializable, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>> implements LogicUseCases.ApplyRule<Q, S, L, A, G, P> {

    private final String logic;
    private final String ruleName;
    private final Proof<P,A,G,S,Q,L> proof;
    private final RuleDao ruleDao;
    private final NaturalDeductionFactory<P,A,G,L,S> naturalDeductionFactory;
    private final ToGoalParser<Goal<Q>,Q,G> toGoalParser;
    private final ToAssmParser<Assumption<Q>,Q,A> toAssmParser;
    private final ToModel<Q,S,L> toModel;
    private final Actions<?,L,?> actions;
    private final Input<Q> inputs;
    private final ExpressionParser<L> parser;

    public ApplyRuleUseCase(String logic, String ruleName, Proof<P, A, G, S, Q, L> proof, RuleDao ruleDao,
                            NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory,
                            ToGoalParser<Goal<Q>, Q, G> toGoalParser,
                            ToAssmParser<Assumption<Q>, Q, A> toAssmParser,
                            ToModel<Q, S, L> toModel,
                            Actions<?,L,?> actions,
                            Input<Q> inputs,
                            ExpressionParser<L> parser){
        this.logic = logic;
        this.ruleName = ruleName;
        this.proof = proof;
        this.ruleDao = ruleDao;
        this.naturalDeductionFactory = naturalDeductionFactory;
        this.toAssmParser = toAssmParser;
        this.toGoalParser = toGoalParser;
        this.toModel = toModel;
        this.actions = actions;
        this.inputs = inputs;
        this.parser = parser;
    }

    @Override
    public Mono<Proof<P, A, G, S, Q, L>> perform() {
        P proofModel = proof.toModelProof(naturalDeductionFactory, toAssmParser, toGoalParser, toModel);
        return ruleDao.<Q>getRule(logic, ruleName).map(rule -> {
            toAction(rule).apply(proofModel);
            return new Proof<>(proofModel.getSteps()
                    .stream()
                    .map(toModel::applyInv)
                    .map(step -> new Step<>(step.expression(), new Reason(step.ruleString().name(), step.ruleString().lines()), step.assmsLevel(), step.extraInformation()))
                    .collect(Collectors.toList()),proof.goal(), proof.name());
        });
    }

    private Action<L,S,P> toAction(Rule<Q> rule){
        if (actions.get().contains(rule.ruleName())){
            var cons = ((Actions<Input<Q>, L, Action<L,S,P>>)actions).getAction(ruleName);
            verify(cons);
            return cons.getCons().apply(inputs, parser);
        } else {
            //TODO custom rules
            throw new UnsupportedOperationException();
        }
    }

    private void verify(Construct<Input<Q>, L, Action<L,S,P>> cons){
        if (cons.isNeedsExpr() && inputs.extraInformation() == null){
            throw new IllegalArgumentException();
        }
        if (cons.isNeedsExpr() && inputs.expression() == null){
            throw new IllegalArgumentException();
        }
        if (cons.getInputs() != inputs.ints().size()){
            throw new IllegalArgumentException();
        }
    }
}
