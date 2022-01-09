package com.dan323.primaryports;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.bean.NaturalDeductionFactory;
import com.dan323.proof.generic.bean.ToAssmParser;
import com.dan323.proof.generic.bean.ToGoalParser;
import com.dan323.proof.generic.bean.ToModel;
import com.dan323.proof.generic.proof.ProofStep;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author danco
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record Proof<P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, S extends ProofStep<L>, T extends Serializable, L extends LogicOperation>(
        @JsonProperty("steps") List<Step<T>> steps, @JsonProperty("goal") Goal<T> goal, @JsonProperty("name") String name) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    public P toModelProof(NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory,
                          ToAssmParser<Assumption<T>,T, A> toAssmsParser,
                          ToGoalParser<Goal<T>,T, G> toGoalParser,
                          ToModel<T, S,L> toModel) {
        P proof = naturalDeductionFactory.get();
        List<S> allSteps = steps.stream().map(toModel).collect(Collectors.toList());
        proof.getSteps().addAll(allSteps);
        proof.setGoal(toGoalParser.apply(goal));
        return proof;
    }

    public P toModelInitialProof(NaturalDeductionFactory<P, A, G, L, S> naturalDeductionFactory,
                                 ToAssmParser<Assumption<T>,T, A> toAssmsParser,
                                 ToGoalParser<Goal<T>,T, G> toGoalParser) {
        P proof = naturalDeductionFactory.get();
        List<A> assms = steps.stream()
                .takeWhile(step -> step.assmsLevel() == 0)
                .takeWhile(step -> step.ruleString().name().contains("Ass"))
                .map(step -> new Assumption<>(step.expression(), step.extraInformation()))
                .map(toAssmsParser)
                .collect(Collectors.toList());
        proof.initializeProof(assms, toGoalParser.apply(goal));
        return proof;
    }

}
