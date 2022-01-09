package com.dan323.primaryports;

import com.dan323.proof.generic.bean.ToAssmParser;
import com.dan323.proof.generic.bean.ToGoalParser;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author danco
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Step<T extends Serializable>(@JsonProperty("expression") String expression, @JsonProperty("reason") Reason ruleString, @JsonProperty("assmsLevel") int assmsLevel, @JsonProperty("extraInformation") T extraInformation) implements Serializable, com.dan323.proof.generic.proof.Step<T>, ToAssmParser.AssumptionSupplier<T>, ToGoalParser.GoalSupplier<T> {

    @Serial
    private static final long serialVersionUID = 114523452L;

}
