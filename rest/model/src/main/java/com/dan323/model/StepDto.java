package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author danco
 */
public record StepDto(String expression, String rule, int assmsLevel, Map<String,String> extraParameters) implements Serializable {

    @Serial
    private static final long serialVersionUID = 114523452L;


}
