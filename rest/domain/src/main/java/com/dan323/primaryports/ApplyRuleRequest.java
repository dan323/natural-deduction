package com.dan323.primaryports;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplyRuleRequest<P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, S extends ProofStep<L>, Q extends Serializable, L extends LogicOperation>(@JsonProperty("proof") Proof<P, A, G, S, Q, L> proof, @JsonProperty("input")Input<Q> input) {
}
