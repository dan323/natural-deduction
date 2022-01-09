package com.dan323.primaryports;

import com.dan323.proof.generic.proof.Step;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Reason(@JsonProperty("name") String name, @JsonProperty("lines") List<Integer> lines) implements Step.StepRule {
}
