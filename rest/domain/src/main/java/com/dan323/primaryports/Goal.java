package com.dan323.primaryports;

import com.dan323.proof.generic.bean.ToGoalParser;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Goal<T>(@JsonProperty("expression") String expression, @JsonProperty("extraInformation") T extraInformation) implements ToGoalParser.GoalSupplier<T> {

    @Override
    public String toString() {
        return expression + "#@#" + extraInformation;
    }
}
