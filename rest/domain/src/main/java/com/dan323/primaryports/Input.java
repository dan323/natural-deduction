package com.dan323.primaryports;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Input<T>(@JsonProperty("lines")List<Integer> ints, @JsonProperty("expression")String expression, @JsonProperty("extraInfo")T extraInformation) implements com.dan323.proof.generic.bean.Input<T> {
}
