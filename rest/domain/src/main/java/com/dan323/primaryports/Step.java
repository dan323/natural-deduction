package com.dan323.primaryports;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author danco
 */
public record Step<T extends Serializable>(String expression, String ruleString, int assmsLevel, T extraInformation) implements Serializable {

    @Serial
    private static final long serialVersionUID = 114523452L;

}
